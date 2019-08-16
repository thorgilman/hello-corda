package net.corda.training;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/* State */
@BelongsToContract(MessageContract.class)
class MessageState implements ContractState {
    final Party origin;
    final Party target;
    final String content = "Hello Corda!";
    public MessageState(Party origin, Party target) { //, String content) {
        this.origin = origin;
        this.target = target;
        //this.content = content;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() { return ImmutableList.of(origin, target); }
}