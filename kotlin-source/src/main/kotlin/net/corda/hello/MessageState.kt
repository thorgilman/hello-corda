package net.corda.hello

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.Party


/** TODO(#1):
 * Remove the default value of 'content' so that its value must be set when creating a new MessageState object. "
 **/

/* State */
 /** BEFORE
@BelongsToContract(MessageContract::class)
data class MessageState(val origin: Party, val target: Party, val content: String = "Hello Corda"): ContractState {
    override val participants = listOf(origin, target)
}
 **/


// /** AFTER
@BelongsToContract(MessageContract::class)
data class MessageState(val origin: Party, val target: Party, val content: String): ContractState {
    override val participants = listOf(origin, target)
}
// **/


