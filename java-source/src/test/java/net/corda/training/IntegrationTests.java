package net.corda.training;

import net.corda.core.concurrent.CordaFuture;
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
        startedNodes.forEach(el -> el.registerInitiatedFlow(HelloCorda.SendMessageFlowResponder.class));
        mockNetwork.runNetwork();
    }


    @After
    public void tearDown() {
        mockNetwork.stopNodes();
    }


    @Test
    public void mytest() throws Exception {
        Party partyA = nodeA.getInfo().getLegalIdentities().get(0);
        Party partyB = nodeB.getInfo().getLegalIdentities().get(0);

        CordaFuture<SignedTransaction> future = nodeA.startFlow(new HelloCorda.SendMessageFlow(partyB));
        mockNetwork.runNetwork();

        SignedTransaction tx = future.get();

    }

}
