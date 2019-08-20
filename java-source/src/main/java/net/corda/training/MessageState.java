package net.corda.training;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/** TODO(#1):
 * Update this code so that the MessageState constructor takes an additional parameter 'content' of type String.
 * This parameter should set the 'content' local parameter of the class.
 **/

/* State */

/** BEFORE
@BelongsToContract(MessageContract.class)
class MessageState implements ContractState {
    final Party origin;
    final Party target;
    final String content = "Hello Corda!";
    public MessageState(Party origin, Party target) {
        this.origin = origin;
        this.target = target;
    }
    @NotNull
    @Override
    public List<AbstractParty> getParticipants() { return ImmutableList.of(origin, target); }
}
 **/

// /** AFTER
@BelongsToContract(MessageContract.class)
class MessageState implements ContractState {
    final Party origin;
    final Party target;
    final String content;
    public MessageState(Party origin, Party target, String content) {
        this.origin = origin;
        this.target = target;
        this.content = content;
    }
    @NotNull
    @Override
    public List<AbstractParty> getParticipants() { return ImmutableList.of(origin, target); }
}
// **/