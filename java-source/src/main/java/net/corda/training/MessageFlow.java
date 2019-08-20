package net.corda.training;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.training.MessageContract.SendMessage;

import java.util.Arrays;
import java.util.Collections;


/** TODO(#2):
 **/

/* Flow */
public class MessageFlow {

    @InitiatingFlow
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private final Party target;
        /* private final String content*/
        public Initiator(Party target /* , String content */) {
            this.target = target;
            /* this.content = content; */
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // Step #1: Get initiating party & notary
            final Party origin = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            // Step #2: Create transaction items
            final MessageState state = new MessageState(origin, target /* , content */);
            final Command<SendMessage> command = new Command<>(new SendMessage(), ImmutableList.of(origin.getOwningKey()));
            final StateAndContract stateAndContract = new StateAndContract(state, MessageContract.ID);

            // Step #3: Create & sign transaction
            final TransactionBuilder builder = new TransactionBuilder(notary).withItems(stateAndContract, command);
            builder.verify(getServiceHub());
            final SignedTransaction stx = getServiceHub().signInitialTransaction(builder);

            // Step #4: Send transaction to other party and finalize
            final FlowSession targetSession = initiateFlow(target);
            return subFlow(new FinalityFlow(stx, Arrays.asList(targetSession)));
        }
    }


    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<SignedTransaction> {

        private final FlowSession otherPartySession;
        public Responder(FlowSession otherPartySession) { this.otherPartySession = otherPartySession; }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return subFlow(new ReceiveFinalityFlow(otherPartySession));
        }
    }
}