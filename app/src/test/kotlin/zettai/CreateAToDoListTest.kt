package zettai

import com.ubertob.pesticide.core.DDT

class CreateAToDoListTest : ZettaiDDT(allActions()) {
    val dylan by NamedActor(::ToDoListOwner)

    @DDT
    fun `The list owner can add new items`() =
        ddtScenario {
            play(
                dylan.`cannot see any lists`(),
                dylan.`can create a new list called #listname`("gardening"),
                dylan.`can create a new list called #listname`("music"),
                dylan.`can see the lists #listNames`(setOf("gardening", "music")),
            )
        }
}
