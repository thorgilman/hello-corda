package net.corda.training

import net.corda.core.node.services.queryBy
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

     /**
     * This test should pass without making any changes to the code.
     * Try running it below.
     * When you are ready to test your solution, comment out this test and uncomment the following test.
     **/
//    @Test
//    fun `baseline test`() {
//         val partyA = nodeA.info.chooseIdentity()
//         val partyB = nodeB.info.chooseIdentity()
//
//         val future = nodeA.startFlow(SendMessageFlow(partyB))
//         mockNetwork.runNetwork()
//         val tx = future.getOrThrow()
//         val state = tx.coreTransaction.outputsOfType<MessageState>().single()
//         assert(state.origin == partyA)
//         assert(state.target == partyB)
//         assert(state.content == "Hello Corda!")
//
//         val nodeAStates = nodeA.services.vaultService.queryBy<MessageState>().states
//         val nodeBStates = nodeB.services.vaultService.queryBy<MessageState>().states
//         assert(nodeAStates.size == 1)
//         assert(nodeBStates.size == 1)
//
//         val stateA = nodeAStates.get(0).state.data
//         val stateB = nodeBStates.get(0).state.data
//         assert(stateA.content == stateB.content)
//         assert(stateA.origin == stateB.origin)
//         assert(stateA.target == stateB.target)
//
//         assert(stateA.content == "Hello Corda!")
//         assert(stateB.content == "Hello Corda!")
//    }

     /**
     * Once you've made your changes, uncomment the test below and run it.
     * If the test passes, your solution is correct!
     **/
    @Test
    fun `answer test`() {
         val partyA = nodeA.info.chooseIdentity()
         val partyB = nodeB.info.chooseIdentity()

         val future = nodeA.startFlow(SendMessageFlow(partyB, "Have a great day!"))
         mockNetwork.runNetwork()
         val tx = future.getOrThrow()
         val state = tx.coreTransaction.outputsOfType<MessageState>().single()
         assert(state.origin == partyA)
         assert(state.target == partyB)
         assert(state.content == "Have a great day!")

         val nodeAStates = nodeA.services.vaultService.queryBy<MessageState>().states
         val nodeBStates = nodeB.services.vaultService.queryBy<MessageState>().states
         assert(nodeAStates.size == 1)
         assert(nodeBStates.size == 1)

         val stateA = nodeAStates.get(0).state.data
         val stateB = nodeBStates.get(0).state.data
         assert(stateA.content == stateB.content)
         assert(stateA.origin == stateB.origin)
         assert(stateA.target == stateB.target)

         assert(stateA.content == "Have a great day!")
         assert(stateB.content == "Have a great day!")
    }



}