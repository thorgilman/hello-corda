package net.corda.training;

import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.node.services.persistence.NodeAttachmentService;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IntegrationTests {

    private MockNetwork mockNetwork;
    private StartedMockNode nodeA, nodeB;

    @Before
    public void setup() {
        MockNetworkParameters mockNetworkParameters = new MockNetworkParameters().withCordappsForAllNodes(
                Arrays.asList(TestCordapp.findCordapp("net.corda.training")));
        mockNetwork = new MockNetwork(mockNetworkParameters);
        System.out.println(mockNetwork);

        nodeA = mockNetwork.createNode(new MockNodeParameters());
        nodeB = mockNetwork.createNode(new MockNodeParameters());

        ArrayList<StartedMockNode> startedNodes = new ArrayList<>();
        startedNodes.add(nodeA);
        startedNodes.add(nodeB);

        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach(el -> el.registerInitiatedFlow(MessageFlow.Responder.class));
        mockNetwork.runNetwork();
    }


    @After
    public void tearDown() {
        mockNetwork.stopNodes();
    }


    @Test
    public void BaselineTest() throws Exception {
        Party partyA = nodeA.getInfo().getLegalIdentities().get(0);
        Party partyB = nodeB.getInfo().getLegalIdentities().get(0);

        CordaFuture<SignedTransaction> future = nodeA.startFlow(new MessageFlow.Initiator(partyB));
        mockNetwork.runNetwork();
        SignedTransaction tx = future.get();

        MessageState state = (MessageState) tx.getTx().getOutputStates().get(0);
        assert(state.content.equals("Hello Corda!"));
        assert(state.origin.equals(partyA));
        assert(state.target.equals(partyB));
        assert(state.getParticipants().equals(ImmutableList.of(partyA, partyB)));

        List<StateAndRef<MessageState>> nodeAStates = nodeA.getServices().getVaultService().queryBy(MessageState.class).getStates();
        List<StateAndRef<MessageState>> nodeBStates = nodeB.getServices().getVaultService().queryBy(MessageState.class).getStates();
        assert(nodeAStates.size() == 1);
        assert(nodeBStates.size() == 1);

        MessageState nodeAState = nodeAStates.get(0).getState().getData();
        MessageState nodeBState = nodeBStates.get(0).getState().getData();
        assert(nodeAState.origin.equals(nodeBState.origin));
        assert(nodeAState.target.equals(nodeBState.target));
        assert(nodeAState.content.equals(nodeBState.content));
    }


    @Test
    public void Task1Checkpoint() {
        Party partyA = nodeA.getInfo().getLegalIdentities().get(0);
        Party partyB = nodeB.getInfo().getLegalIdentities().get(0);
        MessageState state = new MessageState(partyA, partyB, "Hey!");
        assert(state.content.equals("Hey!"));
    }

    @Test
    public void Task2Checkpoint() {
        Party partyA = nodeA.getInfo().getLegalIdentities().get(0);
        Party partyB = nodeB.getInfo().getLegalIdentities().get(0);
        MessageState state = new MessageState(partyA, partyB, "Hey!");
        assert(state.content.equals("Hey!"));
    }



}
