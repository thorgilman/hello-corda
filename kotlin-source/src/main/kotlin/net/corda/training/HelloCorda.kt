package net.corda.training

import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import co.paralleluniverse.fibers.Suspendable as Suspendable


/*
Run network initially
- just puts a note "hello corda" into the initiator's vault
- query to show its there

- then make the message customizable

- then make it sharable
*/


/* Flow */
@InitiatingFlow
@StartableByRPC
class SendMessageFlow(val target: Party) : FlowLogic<SignedTransaction>() { // TODO: Add param 'val messageContent: String'

    @Suspendable
    override fun call(): SignedTransaction {
        // Get initiating party & notary
        val me = serviceHub.myInfo.legalIdentities.first()
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        // Create transaction items
        val command = Command(MessageContract.SendMessage(), listOf(me.owningKey))
        val noteState = MessageState(me, target) // TODO: Change to 'MessageState(me, content=messageContent)'
        val stateAndContract = StateAndContract(noteState, MessageContract.ID)

        // Create & sign transaction
        val utx = TransactionBuilder(notary = notary).withItems(stateAndContract, command)
        val stx = serviceHub.signInitialTransaction(utx)
        stx.verify(serviceHub)

        // Send transaction to other party and finalize
        val targetSession = initiateFlow(target)
        return subFlow(FinalityFlow(stx, listOf(targetSession)))
    }
}
@InitiatedBy(SendMessageFlow::class)
class SendMessageFlowResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}

/* Contract */
class MessageContract: Contract {
    companion object {
        const val ID = "net.corda.training.MessageContract"
    }

    // Commands
    class SendMessage : TypeOnlyCommandData()

    // Contract Constraints
    override fun verify(tx: LedgerTransaction) = requireThat {
        val command = tx.commands.requireSingleCommand<SendMessage>()
        "There should be no input state." using tx.inputStates.isEmpty()
        "There should be one output state." using (tx.outputStates.size == 1)
        "The output state must be a MessageState." using (tx.outputStates.single() is MessageState)

        // TODO: Add a constraint that does "something"!
        // no default message allowed?
    }
}

/* State */
@BelongsToContract(MessageContract::class)
data class MessageState(val origin: Party,
                        val target: Party,
                        val content: String = "Hello Corda!"): ContractState {
    override val participants = listOf(origin, target)
}