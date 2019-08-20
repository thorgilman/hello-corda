package net.corda.training

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

object MessageFlow {

    /* Flow */
/* TODO(#1): Add an additional String parameter to SendMessageFlow so that we can pass a customized message.
    Then pass in this new parameter instead of "Hello World!" when we create our MessageState object. */
    @InitiatingFlow
    @StartableByRPC
    class Initiator(private val target: Party) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            // Step #1: Get initiating party & notary
            val origin = serviceHub.myInfo.legalIdentities.first()
            val notary = serviceHub.networkMapCache.notaryIdentities.single()

            // Step #2: Create transaction items
            val command = Command(MessageContract.SendMessage(), listOf(origin.owningKey))
            val noteState = MessageState(origin, target, "") // ADD CONTENT
            val stateAndContract = StateAndContract(noteState, MessageContract.ID)

            // Step #3: Create & sign transaction
            val builder = TransactionBuilder(notary).withItems(stateAndContract, command)
            val stx = serviceHub.signInitialTransaction(builder)
            stx.verify(serviceHub)

            // Step #4: Send transaction to other party and finalize
            val targetSession = initiateFlow(target)
            return subFlow(FinalityFlow(stx, listOf(targetSession)))
        }
    }
    @InitiatedBy(Initiator::class)
    class Responder(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            return subFlow(ReceiveFinalityFlow(otherPartySession))
        }
    }



}