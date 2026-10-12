package za.co.cttrains.app.data
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import za.co.cttrains.app.BuildConfig
import za.co.cttrains.core.*
class BackendBusProvider(private val service: TransportService) {
    private val json=Json { ignoreUnknownKeys=true };private val http=SourceHttp()
    private fun base(): String { require(BuildConfig.TRANSIT_BASE_URL.isNotBlank()) { "Abeona's backend is not configured. Saved bus data remains available." };return BuildConfig.TRANSIT_BASE_URL }
    suspend fun snapshot(previous: BusSnapshot?,forceMap: Boolean=false): BusSnapshot {
        val value=json.decodeFromString<BusSnapshot>(http.get("${base()}/mobile/v1/providers/${service.name}/snapshot"));require(value.service==service && value.fetchedAt>0 && value.fetchedAt<=System.currentTimeMillis()+60_000);return BusSources.validate(value)
    }
    suspend fun timetable(route: BusRouteDocument,day: String): BusTimetable {
        require(BusSources.officialUrl(route.url,service) && day in listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"))
        val url="${base()}/mobile/v1/providers/${service.name}/timetable".toHttpUrl().newBuilder().addQueryParameter("url",route.url).addQueryParameter("id",route.id).addQueryParameter("title",route.title).addQueryParameter("day",day).addQueryParameter("pdf",route.pdf.toString()).build().toString()
        val value=json.decodeFromString<BusTimetable>(http.get(url));require(value.service==service && value.route.id==route.id && value.day==day && value.fetchedAt>0 && value.fetchedAt<=System.currentTimeMillis()+60_000);return value
    }
}
class BusRepository(context: Context) {
    private val app=context.applicationContext
    val store=FeatureStore(context)
    // Each provider has its own parser, network client and cache identity.
    private val providers: Map<TransportService,BackendBusProvider> = listOf(TransportService.GOLDEN_ARROW,TransportService.MYCITI).associateWith { BackendBusProvider(it) }
    suspend fun refresh(service: TransportService,forceMap: Boolean=false): BusSnapshot {
        require(store.extras().beta.enabled(service) && service!=TransportService.PRASA)
        var previous=store.snapshot(service)
        if(previous==null && service==TransportService.GOLDEN_ARROW) {
            previous=withContext(Dispatchers.Default) { app.assets.open("golden-arrow-survey-stops.json").bufferedReader().use { GoldenSeed.parse(it.readText()) } }
            if(store.extras().beta.enabled(service)) store.saveSnapshot(previous)
        }
        val snapshot=try { providers.getValue(service).snapshot(previous,forceMap) } catch(e: Exception) { if(e is kotlinx.coroutines.CancellationException) throw e;return previous?.copy(notes=previous.notes+"Backend unavailable. Retained data keeps its original check dates.") ?: throw e }
        if(store.extras().beta.enabled(service)) store.saveSnapshot(snapshot)
        return snapshot
    }
    suspend fun timetable(service: TransportService,route: BusRouteDocument,day: String): Pair<BusTimetable,Boolean> {
        require(store.extras().beta.enabled(service))
        try { val result=providers.getValue(service).timetable(route,day);if(!store.extras().beta.enabled(service)) throw kotlinx.coroutines.CancellationException("Operator disabled");store.saveTimetable(result);return result to false }
        catch(e: Exception) { if(e is kotlinx.coroutines.CancellationException) throw e
            return store.timetable(service,route.id,day)?.let { it to true } ?: throw e }
    }
}
