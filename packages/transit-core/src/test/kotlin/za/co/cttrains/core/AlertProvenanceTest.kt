package za.co.cttrains.core
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class AlertProvenanceTest {
    @Test fun legacySnapshotsRemainReadableAndRetainedReportsKeepTheirOriginalTimes() {
        val json=Json { ignoreUnknownKeys=true }
        val legacy=json.decodeFromString<AlertSnapshot>("""{"sourceDate":"2026-10-01","fetchedAt":1000,"alerts":[]}""")
        assertNull(legacy.provenance);assertEquals(1000L,legacy.fetchedAt)
        val cached=json.decodeFromString<AlertSnapshot>("""{"sourceDate":"2026-10-01","fetchedAt":1000,"alerts":[],"provenance":{"provider":"PRASA","kind":"scheduled","fetchedAt":1000,"publishedAt":null,"cached":true,"sourceAvailable":false}}""")
        assertEquals(legacy.sourceDate,cached.sourceDate);assertEquals(legacy.fetchedAt,cached.fetchedAt)
        assertEquals("scheduled",cached.provenance!!.kind);assertTrue(cached.provenance.cached);assertFalse(cached.provenance.sourceAvailable)
    }
}
