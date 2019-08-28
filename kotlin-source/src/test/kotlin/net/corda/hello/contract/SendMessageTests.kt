package net.corda.hello.contract

import net.corda.core.contracts.*
import net.corda.hello.ALICE
import net.corda.hello.BOB
import net.corda.testing.contracts.DummyState
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import net.corda.hello.*
import org.junit.*

/** Contract Tests **/

class SendMessageTests {
    // A pre-defined dummy command.
    class DummyCommand : TypeOnlyCommandData()
    private var ledgerServices = MockServices(listOf("net.corda.hello"))

    @Test
    fun mustIncludeSendMessageCommand() {
        val state = MessageState(ALICE.party, BOB.party, "HEY")
        ledgerServices.ledger {
            transaction {
                output(MessageContract.ID, state)
                command(listOf(ALICE.publicKey, BOB.publicKey), DummyCommand()) // Wrong type.
                this.fails()
            }
            transaction {
                output(MessageContract.ID, state)
                command(listOf(ALICE.publicKey, BOB.publicKey), MessageContract.SendMessage()) // Correct type.
                this.verifies()
            }
        }
    }

    @Test
    fun sendMessageTransactionMustHaveNoInputs() {
        val state = MessageState(ALICE.party, BOB.party, "HEY")
        ledgerServices.ledger {
            transaction {
                input(MessageContract.ID, DummyState())
                command(listOf(ALICE.publicKey, BOB.publicKey), MessageContract.SendMessage())
                output(MessageContract.ID, state)
                this `fails with` "There should be no input state."
            }
            transaction {
                output(MessageContract.ID, state)
                command(listOf(ALICE.publicKey, BOB.publicKey), MessageContract.SendMessage())
                this.verifies() // As there are no input states.
            }
        }
    }

    @Test
    fun sendMessageTransactionMustHaveOneOutput() {
        val state = MessageState(ALICE.party, BOB.party, "HEY")
        ledgerServices.ledger {
            transaction {
                command(listOf(ALICE.publicKey, BOB.publicKey), MessageContract.SendMessage())
                output(MessageContract.ID, state) // Two outputs fails.
                output(MessageContract.ID, state)
                this `fails with` "There should be one output state."
            }
            transaction {
                command(listOf(ALICE.publicKey, BOB.publicKey), MessageContract.SendMessage())
                output(MessageContract.ID, state) // One output passes.
                this.verifies()
            }
        }
    }


    /**
     * Task 5.
     * For obvious reasons, the identity of the lender and borrower must be different.
     * TODO ???
     */
//    @Test
//    fun lenderAndBorrowerCannotBeTheSame() {
//        val iou = IOUState(1.POUNDS, ALICE.party, BOB.party)
//        val borrowerIsLenderIou = IOUState(10.POUNDS, ALICE.party, ALICE.party)
//        ledgerServices.ledger {
//            transaction {
//                command(listOf(ALICE.publicKey, BOB.publicKey),IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, borrowerIsLenderIou)
//                this `fails with` "The lender and borrower cannot have the same identity."
//            }
//            transaction {
//                command(listOf(ALICE.publicKey, BOB.publicKey), IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this.verifies()
//            }
//        }
//    }

    /**
     * Task 6.
     * The list of public keys which the commands hold should contain all of the participants defined in the [IOUState].
     * This is because the IOU is a bilateral agreement where both parties involved are required to sign to issue an
     * IOU or change the properties of an existing IOU.
     * TODO: Add a contract constraint to check that all the required signers are [IOUState] participants.
     * Hint:
     * - In Kotlin you can perform a set equality check of two sets with the == operator.
     * - We need to check that the signers for the transaction are a subset of the participants list.
     * - We don't want any additional public keys not listed in the IOUs participants list.
     * - You will need a reference to the Issue command to get access to the list of signers.
     * - [requireSingleCommand] returns the single required command - you can assign the return value to a constant.
     *
     * Kotlin Hints
     * Kotlin provides a map function for easy conversion of a [Collection] using map
     * - https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/map.html
     * [Collection] can be turned into a set using toSet()
     * - https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/to-set.html
     */
//    @Test
//    fun lenderAndBorrowerMustSignIssueTransaction() {
//        val iou = IOUState(1.POUNDS, ALICE.party, BOB.party)
//        ledgerServices.ledger {
//            transaction {
//                command(DUMMY.publicKey, IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
//            }
//            transaction {
//                command(ALICE.publicKey, IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
//            }
//            transaction {
//                command(BOB.publicKey, IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
//            }
//            transaction {
//                command(listOf(BOB.publicKey, BOB.publicKey, BOB.publicKey), IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
//            }
//            transaction {
//                command(listOf(BOB.publicKey, BOB.publicKey, MINICORP.publicKey, ALICE.publicKey), IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
//            }
//            transaction {
//                command(listOf(BOB.publicKey, BOB.publicKey, BOB.publicKey, ALICE.publicKey), IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this.verifies()
//            }
//            transaction {
//                command(listOf(ALICE.publicKey, BOB.publicKey),IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this.verifies()
//            }
//        }
//    }
}
