package za.co.cttrains.core

import java.io.IOException
import java.util.concurrent.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class RailDataSourceTest {
    private val query = SearchQuery("Unibell", "Salt River", "2026-10-13", "07:00")
    private val checked = 1_792_000_000_000L
    private fun fixture(name: String) = javaClass.getResource("/$name")!!.readText()
    @Test fun `unconfigured backend still fetches a fresh train timetable`() = runBlocking {
        val requests = mutableListOf<String>()
        val source = RailDataSource("", { requests += it; fixture("transfer.html") }, { checked })
        assertEquals(listOf("cttrains-web"), source.sourceIds)
        val entry = source.search(query, "cttrains-api") // Preference written by the broken build.
        assertTrue(entry.journeys.isNotEmpty())
        assertEquals("cttrains-web", entry.sourceId)
        assertEquals(checked, entry.fetchedAt)
        assertEquals(DataProvenance("CT_TRAINS", "scheduled", checked), entry.provenance)
        assertEquals(listOf("https://cttrains.co.za/train-schedule.php?fromStation=Unibell&toStation=Salt+River&travelDate=13%2F10%2F2026&departureTime=07%3A00"), requests)
    }
    @Test fun `stations and service reports work without a backend`() = runBlocking {
        val source = RailDataSource("", { if (it.endsWith("train-form.php")) fixture("catalog.html") else fixture("status2.html") }, { checked })
        assertEquals(102, source.catalog("cttrains-api").stations.size)
        val reports = source.alerts("cttrains-api")
        assertTrue(reports.alerts.isNotEmpty())
        assertEquals(checked, reports.fetchedAt)
        assertFalse(reports.provenance!!.cached)
    }
    @Test fun `failed configured backend falls back to actual web source`() = runBlocking {
        val requests = mutableListOf<String>()
        val source = RailDataSource("https://api.example.test", { requests += it; if (it.startsWith("https://api.")) throw IOException("down"); fixture("transfer.html") }, { checked })
        assertEquals("cttrains-web", source.search(query, "cttrains-api").sourceId)
        assertEquals(2, requests.size)
    }
    @Test fun `healthy configured backend retains its provenance and identity`() = runBlocking {
        val expected = CachedSearch(query, CtTrainsParser.journeys(fixture("transfer.html")), checked, sourceId = "cttrains-api", provenance = DataProvenance("CT_TRAINS", "scheduled", checked, cached = true))
        val requests = mutableListOf<String>()
        val source = RailDataSource("https://api.example.test/", { requests += it; Json.encodeToString(expected) }, { checked })
        assertEquals(expected, source.search(query, "cttrains-api"))
        assertEquals(1, requests.size)
    }
    @Test fun `a wrong backend journey cannot replace the requested train route`() = runBlocking {
        val wrong = CachedSearch(query.copy(to = "Bellville"), emptyList(), checked, sourceId = "cttrains-api")
        val source = RailDataSource("https://api.example.test", { if (it.startsWith("https://api.")) Json.encodeToString(wrong) else fixture("transfer.html") }, { checked })
        assertEquals(query, source.search(query, "cttrains-api").query)
        assertEquals("cttrains-web", source.search(query, "cttrains-api").sourceId)
    }
    @Test fun `cancellation never starts a second source request`() = runBlocking {
        var calls = 0
        val source = RailDataSource("https://api.example.test", { calls++; throw CancellationException() })
        try { source.search(query, "cttrains-api"); fail("Expected cancellation") } catch (_: CancellationException) { }
        assertEquals(1, calls)
    }
    @Test fun `unknown HTML fails visibly rather than inventing an empty timetable`() = runBlocking {
        val source = RailDataSource("", { "<html><body>Service maintenance</body></html>" })
        try { source.search(query, "cttrains-web"); fail("Expected a format error") } catch (_: ScheduleFormatException) { }
    }
}
