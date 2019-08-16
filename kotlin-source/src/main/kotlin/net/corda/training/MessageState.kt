package net.corda.training

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.Party

/* State */
/* TODO(#3): Change the default message from "Hello World!" to "Hello Corda!" */
@BelongsToContract(MessageContract::class)
data class MessageState(val origin: Party, val target: Party, val content: String): ContractState {
    override val participants = listOf(origin, target)
}