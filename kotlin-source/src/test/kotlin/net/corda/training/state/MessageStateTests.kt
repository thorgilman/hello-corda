package net.corda.training.state

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.finance.*
import net.corda.training.ALICE
import net.corda.training.BOB
import net.corda.training.MessageState
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** State Tests **/

class MessageStateTests {

    @Test
    fun hasOriginFieldOfCorrectType() {
        MessageState::class.java.getDeclaredField("origin")
        assertEquals(MessageState::class.java.getDeclaredField("origin").type, Party::class.java)
    }

    @Test
    fun hasTargetFieldOfCorrectType() {
        MessageState::class.java.getDeclaredField("target")
        assertEquals(MessageState::class.java.getDeclaredField("target").type, Party::class.java)
    }

    @Test
    fun hasContentFieldOfCorrectType() {
        MessageState::class.java.getDeclaredField("content")
        assertEquals(MessageState::class.java.getDeclaredField("content").type, String::class.java)
    }
    
    @Test
    fun originIsParticipant() {
        val state = MessageState(ALICE.party, BOB.party, "HEY")
        assertNotEquals(state.participants.indexOf(ALICE.party), -1)
    }

    @Test
    fun targetIsParticipant() {
        val state = MessageState(ALICE.party, BOB.party, "HEY")
        assertNotEquals(state.participants.indexOf(BOB.party), -1)
    }

    /**
        TODO: ?
     */
//    @Test
//    fun isLinearState() {
//        assert(LinearState::class.java.isAssignableFrom(IOUState::class.java))
//    }

    /**
        TODO: ?
     */
//    @Test
//    fun hasLinearIdFieldOfCorrectType() {
//        // Does the linearId field exist?
//        IOUState::class.java.getDeclaredField("linearId")
//        // Is the linearId field of the correct type?
//        assertEquals(IOUState::class.java.getDeclaredField("linearId").type, UniqueIdentifier::class.java)
//    }


    @Test
    fun checkIOUStateParameterOrdering() {
        val fields = MessageState::class.java.declaredFields
        val originIdx = fields.indexOf(MessageState::class.java.getDeclaredField("origin"))
        val targetIdx = fields.indexOf(MessageState::class.java.getDeclaredField("target"))
        val contentIdx = fields.indexOf(MessageState::class.java.getDeclaredField("content"))
        assert(originIdx < targetIdx)
        assert(targetIdx < contentIdx)
    }

}
