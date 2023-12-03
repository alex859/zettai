package zettai

import com.ubertob.pesticide.core.DDT
import com.ubertob.pesticide.core.DomainDrivenTest

typealias ZettaiDDT = DomainDrivenTest<ZettaiActions>

fun allActions() = setOf(DomainOnlyActions(), HttpActions())

class SeeATodoListTest : ZettaiDDT(allActions()) {
    val frank by NamedActor(::ToDoListOwner)
    val bob by NamedActor(::ToDoListOwner)

    val foodToBuy = listOf("carrots", "apples", "milk")
    val shoppingListName = "shopping"
    val gardeningListName = "gardening"
    val gardenItems = listOf("fix the lawn", "water the plants")

    @DDT
    fun `List owners can see their list`() =
        ddtScenario {
            setUp {
                frank.`starts with a list`(shoppingListName, foodToBuy)
                bob.`starts with a list`(gardeningListName, gardenItems)
            }.thenPlay(
                frank.`can see #listname with #itemnames`(shoppingListName, foodToBuy),
                bob.`can see #listname with #itemnames`(gardeningListName, gardenItems),
            )
        }

    @DDT
    fun `Only owners can see their lists`() =
        ddtScenario {
            setUp {
                frank.`starts with a list`(gardeningListName, gardenItems)
                bob.`starts with a list`(shoppingListName, foodToBuy)
            }.thenPlay(
                frank.`cannot see #listname`(shoppingListName),
                bob.`cannot see #listname`(gardeningListName),
            )
        }
}
