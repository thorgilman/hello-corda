package net.corda.training;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import java.util.Arrays;
import java.util.Collections;

public class MessageFlow {

    /* Flow */
    @InitiatingFlow
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private final Party target;
        public Initiator(Party target) {
            this.target = target;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // Get initiating party & notary
            final Party origin = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            // Create transaction items
            final MessageState state = new MessageState(origin, target);
            final Command<MessageContract.SendMessage> command = new Command(new MessageContract.SendMessage(), Collections.singletonList(origin.getOwningKey()));

            // Create & sign transaction
            final StateAndContract stateAndContract = new StateAndContract(state, MessageContract.ID);
            final TransactionBuilder builder = new TransactionBuilder(notary).withItems(stateAndContract, command);
            builder.verify(getServiceHub());
            final SignedTransaction stx = getServiceHub().signInitialTransaction(builder);

            // Send transaction to other party and finalize
            final FlowSession targetSession = initiateFlow(target);
            return subFlow(new FinalityFlow(stx, Arrays.asList(targetSession)));
        }
    }


    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<SignedTransaction> {

        private final FlowSession counterpartySession;

        public Responder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return subFlow(new ReceiveFinalityFlow(counterpartySession));
        }

    }
}