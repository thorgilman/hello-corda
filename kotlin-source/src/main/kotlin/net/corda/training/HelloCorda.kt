package net.corda.training

import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import co.paralleluniverse.fibers.Suspendable as Suspendable

/* Flow */
@InitiatingFlow
@StartableByRPC
/* TODO(#1): Add an additional parameter to SendMessageFlow that so we can pass a customized message */
class SendMessageFlow(private val target: Party /*, private val content: String */) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        // Get initiating party & notary
        val origin = serviceHub.myInfo.legalIdentities.first()
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        // Create transaction items
        val command = Command(MessageContract.SendMessage(), listOf(origin.owningKey))
        /* TODO(#2): Override the content parameter of MessageState with the parameter we created for SendMessageFlow */
        val noteState = MessageState(origin, target /*, content*/)
        val stateAndContract = StateAndContract(noteState, MessageContract.ID)

        // Create & sign transaction
        val builder = TransactionBuilder(notary).withItems(stateAndContract, command)
        val stx = serviceHub.signInitialTransaction(builder)
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

        /* TODO(#3): Add a constraint that doesn't allow you to send messages to yourself! */
//        val outputState = tx.outputStates.single() as MessageState
//        "You cannot send messages to yourself" using (outputState.target != outputState.origin)
    }
}

/* State */
@BelongsToContract(MessageContract::class)
data class MessageState(val origin: Party, val target: Party, val content: String = "Hello Corda!"): ContractState {
    override val participants = listOf(origin, target)
}