package net.corda.hello

import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/* Contract */
/* TODO(#2): Add a constraint that ensures that our transaction has no input state! */
class MessageContract: Contract {

    companion object { const val ID = "net.corda.hello.MessageContract" }
    class SendMessage : TypeOnlyCommandData()

    override fun verify(tx: LedgerTransaction) = requireThat {
        val command = tx.commands.requireSingleCommand<SendMessage>()
        "There should be one output state." using (tx.outputStates.size == 1)
        val outputState = tx.outputStates.single() as MessageState
        "The party sending the message must sign the SendMessage transaction." using (command.signers == listOf(outputState.origin.owningKey))

        // Add this
        // "There should be no input state." using tx.inputStates.isEmpty()
    }
}