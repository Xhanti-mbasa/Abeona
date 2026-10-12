package za.co.cttrains.workers
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.Executors
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import za.co.cttrains.core.*
private val json=Json { ignoreUnknownKeys=true;encodeDefaults=true }
private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
private val providers=listOf(MyCitiProvider(),GoldenArrowProvider()).associateBy { it.service }
private val snapshots=ConcurrentHashMap<TransportService,BusSnapshot>()
private val refreshing=ConcurrentHashMap.newKeySet<TransportService>()
private val failures=ConcurrentHashMap<TransportService,String>()
private val data=Path.of(System.getenv("SCRAPER_DATA_DIR") ?: "../../workers/scrapers/data")
private fun refresh(service: TransportService) {
    if(!refreshing.add(service)) return
    scope.launch {
        try {
            val result=withTimeout(300_000) { providers.getValue(service).snapshot(snapshots[service]) };require(result.routes.isNotEmpty() || result.stops.isNotEmpty());Files.createDirectories(data)
            val file=data.resolve("${service.name}.json");val temp=data.resolve("${service.name}.tmp");Files.writeString(temp,json.encodeToString(result));Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE)
            snapshots[service]=result;failures.remove(service);println("{\"event\":\"provider_refresh\",\"provider\":\"${service.name}\",\"checkedAt\":${result.fetchedAt}}")
        } catch(e: Exception) { failures[service]=e.javaClass.simpleName;System.err.println("{\"event\":\"provider_failure\",\"provider\":\"${service.name}\",\"kind\":\"${e.javaClass.simpleName}\"}") } finally { refreshing.remove(service) }
    }
}
fun main() {
    for(service in providers.keys) { runCatching { json.decodeFromString<BusSnapshot>(Files.readString(data.resolve("${service.name}.json"))) }.getOrNull()?.let { snapshots[service]=BusSources.validate(it) };refresh(service) }
    scope.launch { while(isActive) { delay(24*60*60_000L);providers.keys.forEach(::refresh) } }
    val server=HttpServer.create(InetSocketAddress(System.getenv("SCRAPER_BIND") ?: "127.0.0.1",(System.getenv("SCRAPER_PORT") ?: "8081").toInt()),16);server.executor=Executors.newFixedThreadPool(4)
    server.createContext("/") { exchange ->
        fun reply(status: Int,value: String) { val bytes=value.toByteArray();exchange.responseHeaders.add("Content-Type","application/json; charset=utf-8");exchange.sendResponseHeaders(status,bytes.size.toLong());exchange.responseBody.use { it.write(bytes) } }
        try {
            require(exchange.requestMethod=="GET");val path=exchange.requestURI.path
            val query=exchange.requestURI.rawQuery.orEmpty().split('&').filter { it.contains('=') }.associate { val p=it.split('=',limit=2);URLDecoder.decode(p[0],"UTF-8") to URLDecoder.decode(p[1],"UTF-8") }
            if(path=="/health") reply(200,buildJsonObject { put("status","ok");putJsonObject("providers") { providers.keys.forEach { s->putJsonObject(s.name) { put("available",snapshots[s]!=null && s !in failures);put("refreshing",s in refreshing);put("checkedAt",snapshots[s]?.fetchedAt ?: 0);failures[s]?.let { put("failure",it) } } } } }.toString())
            else if(path=="/alerts") { val value=runBlocking { withTimeout(45_000) { ServiceAlerts.parse(SourceHttp().get(ServiceAlerts.URL,hosts=setOf("cttrains.co.za")),System.currentTimeMillis()) } };reply(200,json.encodeToString(value)) }
            else {
                val parts=path.trim('/').split('/');require(parts.size==2);val service=TransportService.valueOf(parts[0]);val provider=providers.getValue(service)
                when(parts[1]) {
                    "snapshot" -> { val value=snapshots[service];val stale=value==null || System.currentTimeMillis()-value.fetchedAt>24*60*60_000L;if(stale) refresh(service)
                        if(value==null) reply(503,"{\"error\":\"Provider warming or unavailable. Retry later.\"}") else { val row=json.encodeToJsonElement(value).jsonObject.toMutableMap();row["sourceAvailable"]=JsonPrimitive(service !in failures && !stale);reply(200,JsonObject(row).toString()) } }
                    "timetable" -> { val day=query.getValue("day");require(day in listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"));val url=query.getValue("url");require(url.length<=1500 && BusSources.officialUrl(url,service));val route=snapshots[service]?.routes?.firstOrNull { it.url==url } ?: throw IllegalArgumentException("Refresh the catalogue and choose a published route");val value=runBlocking { withTimeout(45_000) { provider.timetable(route,day) } };reply(200,json.encodeToString(value)) }
                    else -> reply(404,"{\"error\":\"Unknown operation\"}")
                }
            }
        } catch(e: IllegalArgumentException) { reply(400,"{\"error\":\"Choose a supported provider, published route and day\"}") }
        catch(e: Exception) { System.err.println("{\"event\":\"request_failure\",\"kind\":\"${e.javaClass.simpleName}\"}");reply(502,"{\"error\":\"Source unavailable. Retained client data remains usable.\"}") } finally { exchange.close() }
    }
    Runtime.getRuntime().addShutdownHook(Thread { scope.cancel();server.stop(1) });server.start();println("Abeona scraper worker listening")
}
