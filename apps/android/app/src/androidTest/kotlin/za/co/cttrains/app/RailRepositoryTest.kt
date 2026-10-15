package za.co.cttrains.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import za.co.cttrains.app.data.*
import za.co.cttrains.core.*

@RunWith(AndroidJUnit4::class)
class RailRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val query = SearchQuery("Unibell", "Salt River", "2026-10-13", "07:00")
    private fun fixture(name: String) = InstrumentationRegistry.getInstrumentation().context.assets.open(name).bufferedReader().use { it.readText() }
    @Before fun clearCache() = runBlocking { TrainRepository(context).clearCache() }
    @After fun cleanup() = clearCache()
    @Test fun freshUnconfiguredSearchIsPersistedWithItsRealSource() = runBlocking {
        var calls = 0
        val source = RailDataSource("", { calls++; fixture("transfer.html") })
        val repository = TrainRepository(context, source)
        assertNull(repository.store.cached(query, "cttrains-web"))
        val result = repository.search(query, "cttrains-api")
        assertEquals(1, calls); assertFalse(result.offline)
        assertTrue(result.entry.journeys.isNotEmpty())
        assertEquals("cttrains-web", result.entry.sourceId)
        assertEquals(result.entry, TrainRepository(context).store.cached(query, "cttrains-web"))
    }
    @Test fun outageUsesOnlyTheExactQueryAndKeepsItsOriginalTimestamp() = runBlocking {
        val saved = CachedSearch(query, CtTrainsParser.journeys(fixture("transfer.html")), System.currentTimeMillis() - 60000, sourceId = "cttrains-web")
        val repository = TrainRepository(context, RailDataSource("", { throw IOException("offline") }))
        repository.store.cache(saved)
        val result = repository.search(query, "cttrains-api")
        assertTrue(result.offline); assertEquals(saved.fetchedAt, result.entry.fetchedAt)
        assertTrue(result.entry.provenance!!.cached); assertFalse(result.entry.provenance!!.sourceAvailable)
        try { repository.search(query.copy(time = "08:00"), "cttrains-api"); fail("A different timetable must not be reused") } catch (_: IOException) { }
    }
}
