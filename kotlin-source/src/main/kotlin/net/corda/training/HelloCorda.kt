package net.corda.training

import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.training.MessageContract.SendMessage
import co.paralleluniverse.fibers.Suspendable as Suspendable

/* Flow */
/* TODO(#1): Add an additional String parameter to SendMessageFlow so that we can pass a customized message.
    Then pass in this new parameter instead of "Hello World!" when we create our MessageState object. */
@InitiatingFlow
@StartableByRPC
class SendMessageFlow(private val target: Party) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        // Get initiating party & notary
        val origin = serviceHub.myInfo.legalIdentities.first()
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        // Create transaction items
        val command = Command(SendMessage(), listOf(origin.owningKey))
        val noteState = MessageState(origin, target, "") // ADD CONTENT
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
/* TODO(#2): Add a constraint that ensures that our transaction has no input state! */
class MessageContract: Contract {
    companion object { const val ID = "net.corda.training.MessageContract" }

    // Commands
    class SendMessage : TypeOnlyCommandData()

    // Contract Clause
    override fun verify(tx: LedgerTransaction) = requireThat {
        val command = tx.commands.requireSingleCommand<SendMessage>()

        "There should be one output state." using (tx.outputStates.size == 1)
        val outputState = tx.outputStates.single() as MessageState
        "The party sending the message must sign the SendMessage transaction" using (command.signers == listOf(outputState.origin).map{ it.owningKey })

        // Add this
        // "There should be no input state." using tx.inputStates.isEmpty()
    }
}

/* State */
/* TODO(#3): Change the default message from "Hello World!" to "Hello Corda!" */
@BelongsToContract(MessageContract::class)
data class MessageState(val origin: Party, val target: Party, val content: String): ContractState {
    override val participants = listOf(origin, target)
}