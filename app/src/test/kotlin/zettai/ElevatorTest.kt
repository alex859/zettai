package zettai

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ElevatorTest {
    @Test
    fun `elevator goes to first floor`() {
        val result = elevatorHandler(GoToFloor(floor = 2), WaitingAt(floor = 1))

        expectThat(result).isEqualTo(TravelingTo(floor = 2))
    }

    @Test
    fun `call elevator that is already at this floor`() {
        val result = elevatorHandler(CallElevator(floor = 2), WaitingAt(floor = 2))

        expectThat(result).isEqualTo(WaitingAt(floor = 2))
    }

    @Test
    fun `call elevator waiting at another floor`() {
        val result = elevatorHandler(CallElevator(floor = 2), WaitingAt(floor = 5))

        expectThat(result).isEqualTo(TravelingTo(floor = 2))
    }

    @Test
    fun `call elevator traveling to another floor`() {
        val result = elevatorHandler(CallElevator(floor = 2), TravelingTo(floor = 5))

        expectThat(result).isEqualTo(TravelingTo(floor = 5))
    }

    @Test
    fun `go to floor 4 while traveling to floor 4`() {
        val result = elevatorHandler(GoToFloor(floor = 2), TravelingTo(floor = 2))

        expectThat(result).isEqualTo(TravelingTo(floor = 2))
    }

    @Test
    fun `go to floor 4 while traveling to floor 5`() {
        val result = elevatorHandler(GoToFloor(floor = 5), TravelingTo(floor = 2))

        expectThat(result).isEqualTo(TravelingTo(floor = 2))
    }
}

val elevatorHandler: ElevatorCommandHandler = { command, state ->
    when (state) {
        is WaitingAt ->
            when (command) {
                is GoToFloor -> TravelingTo(command.floor)
                is CallElevator ->
                    if (state.floor == command.floor) {
                        state
                    } else {
                        TravelingTo(command.floor)
                    }
            }
        is TravelingTo ->
            when (command) {
                is GoToFloor -> state
                is CallElevator -> state
            }
    }
}

sealed interface ElevatorCommand

typealias Floor = Int

data class CallElevator(val floor: Floor) : ElevatorCommand

data class GoToFloor(val floor: Floor) : ElevatorCommand

sealed interface ElevatorState

data class TravelingTo(val floor: Floor) : ElevatorState

data class WaitingAt(val floor: Floor) : ElevatorState

typealias ElevatorCommandHandler = (ElevatorCommand, ElevatorState) -> ElevatorState
