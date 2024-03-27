package zettai

import com.ubertob.pesticide.core.DDT

class UserListsPageDDT : ZettaiDDT(allActions()) {
    val carol by NamedActor(::ToDoListOwner)
    val emma by NamedActor(::ToDoListOwner)

    @DDT
    fun `new user has no lists`() =
        ddtScenario {
            play(
                emma.`cannot see any lists`(),
            )
        }

    @DDT
    fun `only owners can see their lists`() =
        ddtScenario {
            val expectedLists = generateSomeToDoLists()

            setUp {
                carol.`starts with some lists`(expectedLists)
            }.thenPlay(
                carol.`can see the lists #listNames`(expectedLists.keys),
                emma.`cannot see any lists`(),
            )
        }

    private fun generateSomeToDoLists() =
        mapOf(
            "work" to listOf("meeting", "spreadsheet"),
            "home" to listOf("buy food"),
            "friends" to listOf("buy presents"),
        )
}
