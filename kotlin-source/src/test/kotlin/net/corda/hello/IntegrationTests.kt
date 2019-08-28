package net.corda.hello

import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.internal.chooseIdentity
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class IntegrationTests {

    private val mockNetwork = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(TestCordapp.findCordapp("net.corda.hello"))))
    private lateinit var nodeA: StartedMockNode
    private lateinit var nodeB: StartedMockNode

    @Before
    fun setup() {
        nodeA = mockNetwork.createNode(MockNodeParameters())
        nodeB = mockNetwork.createNode(MockNodeParameters())
        listOf(nodeA, nodeB).forEach { it.registerInitiatedFlow(MessageFlow.Responder::class.java) }
    }

    @After
    fun tearDown() = mockNetwork.stopNodes()

     /**
     * This test should pass without making any changes to the code.
     * Try running it below.
     * When you are ready to test your solution, comment out this test and uncomment the following test.
     **/
    @Test
    fun `Baseline Test`() {
         val partyA = nodeA.info.chooseIdentity()
         val partyB = nodeB.info.chooseIdentity()

         val future = nodeA.startFlow(MessageFlow.Initiator(partyB))
         mockNetwork.runNetwork()
         val tx = future.getOrThrow()

         val state = tx.coreTransaction.outputsOfType<MessageState>().single()
         assert(state.origin == partyA)
         assert(state.target == partyB)
         assert(state.content == "Hello World!")

         val nodeAStates = nodeA.services.vaultService.queryBy<MessageState>().states
         val nodeBStates = nodeB.services.vaultService.queryBy<MessageState>().states
         assert(nodeAStates.size == 1)
         assert(nodeBStates.size == 1)

         val stateA = nodeAStates.single().state.data
         val stateB = nodeBStates.single().state.data
         assert(stateA.origin == stateB.origin)
         assert(stateA.target == stateB.target)
         assert(stateA.content == stateB.content)
    }

     /**
     **/
    @Test
    fun `Completed Test`() {
         val partyA = nodeA.info.chooseIdentity()
         val partyB = nodeB.info.chooseIdentity()

         /* Test Task #1 */
        // ...

         /* Test Task #2 */




         /* Test Task #3 */





//         val future = nodeA.startFlow(MessageFlow.Initiator(partyB, "Hello Corda!"))
//         mockNetwork.runNetwork()
//         val tx = future.getOrThrow()
//         val state = tx.coreTransaction.outputsOfType<MessageState>().single()
//
//         val nodeAStates = nodeA.services.vaultService.queryBy<MessageState>().states
//         val nodeBStates = nodeB.services.vaultService.queryBy<MessageState>().states
//         assert(nodeAStates.size == 1)
//         assert(nodeBStates.size == 1)
//
//         val stateA = nodeAStates.get(0).state.data
//         val stateB = nodeBStates.get(0).state.data
//
//         assert(stateA.content == "Hello Corda!")
//         assert(stateB.content == "Hello Corda!")
    }




}