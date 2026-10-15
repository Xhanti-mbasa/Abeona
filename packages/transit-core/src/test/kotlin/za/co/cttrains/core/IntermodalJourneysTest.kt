package za.co.cttrains.core
import org.junit.Test
import org.junit.Assert.*
class IntermodalJourneysTest {
    @Test fun transferIncludesAccessWaitingAndFinalWalk() {
        val from=TripEstimate(10*60_000,30*60_000,30,5,0,true)
        val to=TripEstimate(40*60_000,65*60_000,35,8,2,true)
        val joined=IntermodalJourneys.connect(from,to,0)!!
        assertEquals(65,joined.totalMinutes);assertEquals(13,joined.walkingMinutes);assertEquals(2,joined.waitingMinutes)
    }
    @Test fun cannotInventUnknownWalkOrMissedConnection() {
        val from=TripEstimate(600_000,1_800_000,30,5,0,true)
        val to=TripEstimate(2_400_000,3_900_000,35,8,2,true)
        assertNull(IntermodalJourneys.connect(from,to.copy(departureAt=1_700_000),0))
        assertNull(IntermodalJourneys.connect(from,to.copy(walkingMinutes=null),0))
        assertNull(IntermodalJourneys.connect(from,to.copy(canCatch=false),0))
    }
    @Test fun rejectUnboundedOrChronologicallyInvalidJourney() {
        val from=TripEstimate(600_000,1_800_000,30,5,0,true)
        assertNull(IntermodalJourneys.connect(from,TripEstimate(2_400_000,32_400_000,500,8,0,true),0))
        assertNull(IntermodalJourneys.connect(from.copy(arrivalAt=300_000),TripEstimate(2_400_000,3_900_000,35,8,2,true),0))
    }
}
