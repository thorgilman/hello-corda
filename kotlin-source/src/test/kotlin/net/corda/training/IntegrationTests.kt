package net.corda.training

import net.corda.core.utilities.getOrThrow
import net.corda.testing.internal.chooseIdentity
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class IntegrationTests {

    private val mockNetwork = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(TestCordapp.findCordapp("net.corda.training"))))
    private lateinit var nodeA: StartedMockNode
    private lateinit var nodeB: StartedMockNode

    @Before
    fun setup() {
        nodeA = mockNetwork.createNode(MockNodeParameters())
        nodeB = mockNetwork.createNode(MockNodeParameters())
        listOf(nodeA, nodeB).forEach {
            it.registerInitiatedFlow(SendMessageFlowResponder::class.java)
        }
    }

    @After
    fun tearDown() = mockNetwork.stopNodes()

    @Test
    fun `my test`() {
        val partyA = nodeA.info.chooseIdentity()
        val partyB = nodeB.info.chooseIdentity()

        val future = nodeA.startFlow(SendMessageFlow(partyB, "HEY"))
        mockNetwork.runNetwork()
        val tx = future.getOrThrow()
        val state = tx.coreTransaction.outputsOfType<MessageState>().single()
        assert(state.origin == partyA)
        assert(state.target == partyB)
        assert(state.content == "HEY")
    }



}