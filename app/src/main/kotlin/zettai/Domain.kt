package zettai

import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

data class ToDoList(val listName: ListName, val items: List<ToDoItem>)

data class ListName internal constructor(val name: String) {
    companion object {
        private val validPattern = "[A-Za-z0-9-]+".toRegex()

        fun fromTrusted(name: String): ListName = ListName(name)

        @Suppress("MagicNumber")
        fun fromUntrusted(name: String): ListName? =
            if (name.matches(validPattern) && name.length in 1..40) {
                ListName(name)
            } else {
                null
            }
    }
}

data class ToDoItem(val description: String, val dueDate: LocalDate? = null, val status: ToDoStatus = ToDoStatus.Todo)

enum class ToDoStatus { Todo, InProgress, Done, Blocked }

data class User(val name: String)

sealed interface TodoListCommand

data class CreateToDoList(val user: User, val name: ListName) : TodoListCommand {
    val id = TodoListId.mint()
}

data class AddToDoItem(val user: User, val listName: ListName, val item: ToDoItem) : TodoListCommand

typealias TodoListId = EntityId

data class EntityId(val raw: UUID) {
    companion object {
        fun mint() = EntityId(UUID.randomUUID())
    }
}

interface EntityEvent {
    val id: EntityId
}

sealed class ToDoListEvent : EntityEvent

data class ListCreated(
    override val id: TodoListId,
    val owner: User,
    val name: ListName,
) : ToDoListEvent()

data class ItemAdded(
    override val id: TodoListId,
    val item: ToDoItem,
) : ToDoListEvent()

data class ItemRemoved(
    override val id: TodoListId,
    val item: ToDoItem,
) : ToDoListEvent()

data class ItemModified(
    override val id: TodoListId,
    val previousItem: ToDoItem,
    val item: ToDoItem,
) : ToDoListEvent()

data class ListPutOnHold(
    override val id: TodoListId,
    val reason: String,
) : ToDoListEvent()

data class ListReleased(
    override val id: TodoListId,
) : ToDoListEvent()

data class ListClosed(
    override val id: TodoListId,
    val closeOn: Instant,
) : ToDoListEvent()

interface EntityState<in E : EntityEvent> {
    fun combine(event: E): EntityState<E>
}

sealed class TodoListState : EntityState<ToDoListEvent> {
    abstract override fun combine(event: ToDoListEvent): TodoListState
}

data object InitialState : TodoListState() {
    override fun combine(event: ToDoListEvent) =
        when (event) {
            is ListCreated -> ActiveToDoList(event.id, event.owner, event.name, emptyList())
            else -> this
        }
}

data class ActiveToDoList internal constructor(
    val id: TodoListId,
    val user: User,
    val name: ListName,
    val items: List<ToDoItem>,
) : TodoListState() {
    override fun combine(event: ToDoListEvent): TodoListState =
        when (event) {
            is ItemAdded -> copy(items = items + event.item)
            is ItemRemoved -> copy(items = items - event.item)
            is ItemModified -> copy(items = items - event.previousItem + event.item)
            is ListPutOnHold -> OnHoldToDoList(id, user, name, items, event.reason)
            is ListClosed -> ClosedToDoList(id, user, name, items)
            else -> this
        }
}

data class OnHoldToDoList internal constructor(
    val id: TodoListId,
    val user: User,
    val listName: ListName,
    val items: List<ToDoItem>,
    val reason: String,
) : TodoListState() {
    override fun combine(event: ToDoListEvent): TodoListState =
        when (event) {
            is ListReleased -> ActiveToDoList(id, user, listName, items)
            else -> this
        }
}

data class ClosedToDoList internal constructor(
    val id: TodoListId,
    val user: User,
    val listName: ListName,
    val items: List<ToDoItem>,
) : TodoListState() {
    override fun combine(event: ToDoListEvent): TodoListState = this
}

fun Iterable<ToDoListEvent>.fold(): TodoListState = fold(InitialState as TodoListState) { acc, e -> acc.combine(e) }

typealias CommandHandler<CMD, EVENT> = (CMD) -> List<EVENT>?

class ToDoListCommandHandler(
    val retriever: ToDoListRetriever,
    val readModel: ToDoListUpdatableFetcher,
) : (TodoListCommand) -> List<ToDoListEvent>? {
    override fun invoke(command: TodoListCommand): List<ToDoListEvent>? =
        when (command) {
            is CreateToDoList -> command.execute()
            is AddToDoItem -> command.execute()
        }

    private fun CreateToDoList.execute(): List<ToDoListEvent>? =
        retriever.retrieveByName(user, name)?.let { state ->
            when (state) {
                InitialState -> {
                    readModel.assignListToUser(user, ToDoList(name, emptyList()))
                    listOf(ListCreated(id, user, name))
                }

                else -> null
            }
        }

    private fun AddToDoItem.execute(): List<ToDoListEvent>? {
        return retriever.retrieveByName(user, listName)
            ?.let { listState ->
                when (listState) {
                    is ActiveToDoList -> {
                        if (listState.items.any { it.description == item.description }) {
                            null
                        } else {
                            readModel.addItemToList(user, listState.name, item)
                            listOf(ItemAdded(listState.id, item))
                        }
                    }
                    InitialState, is OnHoldToDoList, is ClosedToDoList -> null
                }
            }
    }
}

fun interface ToDoListRetriever {
    fun retrieveByName(
        user: User,
        listName: ListName,
    ): TodoListState?
}

typealias EventStreamer<T> = (EntityId) -> List<T>?

typealias EventPersister<T> = (List<T>) -> List<T>

class ToDoListEventStore(val eventStreamer: ToDoListEventStreamer) : ToDoListRetriever, EventPersister<ToDoListEvent> {
    override fun retrieveByName(
        user: User,
        listName: ListName,
    ): TodoListState? =
        eventStreamer
            .retrieveIdFromName(user, listName)
            ?.let(::retrieveById)
            ?: InitialState

    override fun invoke(events: List<ToDoListEvent>): List<ToDoListEvent> {
        return eventStreamer.store(events)
    }

    private fun retrieveById(id: TodoListId): TodoListState? = eventStreamer(id)?.fold()
}

interface ToDoListEventStreamer : EventStreamer<ToDoListEvent> {
    fun retrieveIdFromName(
        user: User,
        listName: ListName,
    ): TodoListId?

    fun store(newEvents: Iterable<ToDoListEvent>): List<ToDoListEvent>
}

class ToDoListEventStreamerInMemory : ToDoListEventStreamer {
    val events =
        AtomicReference<List<ToDoListEvent>>(
            emptyList(),
        )

    override fun retrieveIdFromName(
        user: User,
        listName: ListName,
    ): TodoListId? =
        events.get()
            .filterIsInstance<CreateToDoList>()
            .firstOrNull { it.name == listName && it.user == user }
            ?.id

    override fun store(newEvents: Iterable<ToDoListEvent>): List<ToDoListEvent> {
        return events.getAndUpdate { it + newEvents }
    }

    override fun invoke(id: EntityId): List<ToDoListEvent>? {
        return events.get().filter { it.id == id }
    }
}
