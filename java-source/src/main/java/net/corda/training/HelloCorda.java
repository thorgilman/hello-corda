package net.corda.training;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import org.jetbrains.annotations.NotNull;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

/* Flow */
public class HelloCorda {

    @InitiatingFlow
    public static class SendMessageFlow extends FlowLogic<SignedTransaction> {
        private final Party target;
        public SendMessageFlow(Party target) { this.target = target; }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // Get initiating party & notary
            final Party origin = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            // Create transaction items
            final MessageState state = new MessageState(origin, target, "Hello Corda!");
            final Command<MessageContract.Commands.SendMessage> command = new Command<>(
                new MessageContract.Commands.SendMessage(),
                Arrays.asList(origin.getOwningKey())
            );

            // Create & sign transaction
            final TransactionBuilder builder = new TransactionBuilder(notary);
            builder.addOutputState(state, MessageContract.ID);
            builder.addCommand(command);

            //builder.verify(getServiceHub());

            final SignedTransaction stx = getServiceHub().signInitialTransaction(builder);



            // Send transaction to other party and finalize
            final FlowSession targetSession = initiateFlow(target);
            return subFlow(new FinalityFlow(stx, Arrays.asList(targetSession)));
        }
    }


    @InitiatedBy(SendMessageFlow.class)
    public static class SendMessageFlowResponder extends FlowLogic<SignedTransaction> {

        private final FlowSession counterpartySession;
        public SendMessageFlowResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return subFlow(new ReceiveFinalityFlow(counterpartySession));
        }
    }
}


/* Contract */
class MessageContract implements Contract {

    public static final String ID = "net.corda.training.MessageContract";

    interface Commands extends CommandData {
        class SendMessage extends TypeOnlyCommandData implements Commands{}
    }

    @Override
    public void verify(LedgerTransaction tx) {

        final Commands commandData = requireSingleCommand(tx.getCommands(), Commands.class).getValue();
//        requireThat( require -> {
//            require.using("Must be a SendMessage command", commandData.equals(new Commands.SendMessage()));
//            require.using("There should be no input state.", tx.getInputStates().isEmpty());
//            require.using("There should one output state.", tx.getOutputStates().size() == 1);
//            require.using("The output state must be a MessageState.", tx.getOutputStates().get(0) instanceof MessageState);
//
//            // TODO: Add a constraint that doesn't allow a message to be sent with the default content
//            return null;
//        });
    }
}

/* State */
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




