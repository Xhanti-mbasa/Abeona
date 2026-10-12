package za.co.cttrains.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import za.co.cttrains.app.BuildConfig
import za.co.cttrains.core.*

@Serializable
data class Preferences(
    val routes: List<SavedRoute> = emptyList(), val favoriteStations: List<String> = emptyList(),
    val theme: String = "System", val dynamicColor: Boolean = false, val lastQuery: SearchQuery? = null,
    val profileId: String = "", val sourceId: String = "cttrains-web", val mapMode: String = "online",
    val offlineMapId: Long? = null,
    val fareCorrections: List<FareCorrection> = emptyList(),
)
data class SearchResult(val entry: CachedSearch, val offline: Boolean, val restoredPlan: Boolean = false)
data class TransitSource(val id: String, val label: String, val description: String)

/** A normalized data boundary; UI never scrapes or mixes sources. */
class TrainRepository(private val context: Context, private val source: RailDataSource = RailDataSource(BuildConfig.TRANSIT_BASE_URL, RailHttp::get)) {
    val store = LocalTransitStore.get(context)
    private val json = Json { ignoreUnknownKeys = true }
    val sources = source.sourceIds.map { id -> if (id == "cttrains-api")
        TransitSource(id, "Abeona backend", "Scheduled rail data; CT Trains is the fallback if the server is unavailable")
        else TransitSource(id, "CT Trains", "Public community train timetables and service reports") }
    val defaultSourceId: String get() = source.availableSource("cttrains-api")
    val preferences = flow { store.initialize(); emitAll(store.preferences) }
    suspend fun updatePreferences(transform: (Preferences) -> Preferences) = store.updatePreferences(transform)
    suspend fun bundledCatalog(): StationCatalog = withContext(Dispatchers.IO) {
        context.assets.open("stations.json").bufferedReader().use { json.decodeFromString(it.readText()) }
    }
    suspend fun storedCatalog(): StationCatalog? = store.catalog()
    suspend fun refreshCatalog(sourceId: String = "cttrains-web", force: Boolean = true): StationCatalog {
        val checked = store.readLocal("catalog-fetched-at")?.toLongOrNull()
        if (!force && checked != null && System.currentTimeMillis() - checked in 0 until 24 * 60 * 60_000L) {
            store.catalog()?.let { return it }
        }
        val catalog = withContext(Dispatchers.Default) { source.catalog(sourceId) }
        require(catalog.stations.size >= 2) { "Transit source returned an empty station list." }
        store.saveCatalog(catalog)
        store.writeLocal("catalog-fetched-at", System.currentTimeMillis().toString())
        return catalog
    }
    fun websiteUrl(query: SearchQuery): String = RailDataSource.websiteUrl(query)
    suspend fun search(query: SearchQuery, sourceId: String): SearchResult {
        require(sourceId in setOf("cttrains-api","cttrains-web")) { "This transit source is unavailable." }
        try {
            val entry = withContext(Dispatchers.Default) { source.search(query, sourceId) }
            store.cache(entry)
            return SearchResult(entry,entry.provenance?.cached==true || entry.provenance?.sourceAvailable==false)
        } catch (error: Exception) {
            if (error is kotlinx.coroutines.CancellationException) throw error
            return (store.cached(query,sourceId) ?: store.cached(query,"cttrains-api") ?: store.cached(query,"cttrains-web"))?.let {
                SearchResult(it.copy(provenance = (it.provenance ?: DataProvenance("CT_TRAINS", "scheduled", it.fetchedAt)).copy(cached = true, sourceAvailable = false)),true)
            } ?: throw error
        }
    }
    suspend fun clearCache() = store.clearCache()
    suspend fun savedAlerts(): AlertSnapshot? = store.alerts()
    suspend fun refreshAlerts(): AlertSnapshot {
        val snapshot = withContext(Dispatchers.Default) { source.alerts(store.preferences.first().sourceId) }
        store.saveAlerts(snapshot)
        return snapshot
    }
    private var railRouter: RailRouter? = null
    suspend fun trace(journey: Journey, stations: List<StationPosition>): JourneyTrace = withContext(Dispatchers.Default) {
        val router = railRouter ?: context.assets.open("rail-network.json").bufferedReader().use { RailRouter(json.decodeFromString<RailNetwork>(it.readText())) }.also { railRouter = it }
        router.trace(journey, stations)
    }
}
