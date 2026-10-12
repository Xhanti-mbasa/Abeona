package za.co.cttrains.core

import java.net.URLEncoder
import java.util.concurrent.CancellationException
import kotlinx.serialization.json.Json

/** Public rail pages remain usable before an Abeona server has been provisioned. */
class RailDataSource(
    private val backend: String,
    private val get: suspend (String) -> String,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val json = Json { ignoreUnknownKeys = true }
    val sourceIds: List<String> = listOf("cttrains-web") + if (backend.isNotBlank()) listOf("cttrains-api") else emptyList()
    fun availableSource(requested: String): String = requested.takeIf { it in sourceIds } ?: "cttrains-web"

    suspend fun catalog(source: String): StationCatalog = fromSource(source, {
        json.decodeFromString<StationCatalog>(get("${backend.trimEnd('/')}/mobile/v1/catalog")).also {
            if (it.stations.size < 2) throw ScheduleFormatException("The station list could not be read.")
        }
    }, { CtTrainsParser.catalog(get("https://cttrains.co.za/train-form.php")) })

    suspend fun search(query: SearchQuery, source: String): CachedSearch = fromSource(source, {
        val url = url("${backend.trimEnd('/')}/mobile/v1/journeys", listOf("from" to query.from, "to" to query.to, "date" to query.date, "time" to query.time))
        validateTransitEntry(json.decodeFromString<CachedSearch>(get(url)), query, "cttrains-api", now())
    }, {
        val html = get(websiteUrl(query))
        val checked = now()
        validateTransitEntry(CachedSearch(query, CtTrainsParser.journeys(html, query.time), checked,
            CtTrainsParser.fares(html), "cttrains-web", DataProvenance("CT_TRAINS", "scheduled", checked)), query, "cttrains-web", checked)
    })

    suspend fun alerts(source: String): AlertSnapshot = fromSource(source, {
        json.decodeFromString<AlertSnapshot>(get("${backend.trimEnd('/')}/mobile/v1/alerts")).also {
            if (it.fetchedAt <= 0 || it.fetchedAt > now() + 60_000) throw ScheduleFormatException("The service update timestamp could not be read.")
        }
    }, {
        val checked = now()
        ServiceAlerts.parse(get(ServiceAlerts.URL), checked).copy(provenance = DataProvenance("CT_TRAINS", "reported", checked))
    })

    private suspend fun <T> fromSource(source: String, api: suspend () -> T, web: suspend () -> T): T {
        if (availableSource(source) != "cttrains-api") return web()
        return try { api() } catch (error: Exception) {
            if (error is CancellationException) throw error
            // A missing or failed backend must never disable the previously working train source.
            web()
        }
    }

    companion object {
        fun websiteUrl(query: SearchQuery): String = url("https://cttrains.co.za/train-schedule.php", listOf(
            "fromStation" to query.from, "toStation" to query.to, "travelDate" to query.websiteDate, "departureTime" to query.time))
        private fun url(base: String, values: List<Pair<String, String>>): String = base + "?" + values.joinToString("&") {
            "${it.first}=${URLEncoder.encode(it.second, "UTF-8")}"
        }
    }
}
