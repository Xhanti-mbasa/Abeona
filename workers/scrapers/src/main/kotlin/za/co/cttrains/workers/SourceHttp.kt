package za.co.cttrains.workers
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.*
class HttpStatusException(val status: Int): IOException("Source HTTP $status")
class SourceHttp(cookies: Boolean=false) {
    companion object { private val gates=ConcurrentHashMap<String,Mutex>();private val next=ConcurrentHashMap<String,Long>() }
    private val jar=object: CookieJar {
        private val values=mutableListOf<Cookie>()
        @Synchronized override fun saveFromResponse(url: HttpUrl,cookies: List<Cookie>) { values.removeAll { it.expiresAt<System.currentTimeMillis() };cookies.forEach { c->values.removeAll { it.name==c.name && it.domain==c.domain };values+=c };while(values.size>100) values.removeAt(0) }
        @Synchronized override fun loadForRequest(url: HttpUrl)=values.filter { it.matches(url) && it.expiresAt>System.currentTimeMillis() }
    }
    private val client=OkHttpClient.Builder().connectTimeout(8,TimeUnit.SECONDS).readTimeout(15,TimeUnit.SECONDS).callTimeout(20,TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false).cookieJar(if(cookies) jar else CookieJar.NO_COOKIES).build()
    suspend fun bytes(request: Request,limit: Int=8*1024*1024,allowedHosts: Set<String>?=null): ByteArray {
        require(limit in 1..12*1024*1024 && allowedHosts!=null);var current=request
        repeat(5) {
            require(current.url.isHttps && current.url.username.isBlank() && current.url.password.isBlank() && current.url.host in allowedHosts)
            val host=current.url.host
            val response=gates.computeIfAbsent(host) { Mutex() }.withLock {
                val pause=(next[host] ?: 0)-System.currentTimeMillis();if(pause>0) delay(pause.coerceAtMost(60_000))
                try { withContext(Dispatchers.IO) { client.newCall(current.newBuilder().header("User-Agent","Abeona-Connector/1.11 (https://github.com/Xhanti-mbasa/Abeona; community tool)").build()).execute() } } finally { next[host]=System.currentTimeMillis()+1100 }
            }
            response.use { r ->
                if(r.code in 301..308) { val target=r.header("Location")?.let(current.url::resolve) ?: throw IOException("Missing redirect");require(target.isHttps && target.host in allowedHosts);current=current.newBuilder().url(target).build() }
                else {
                    if(r.code==429) { next[host]=System.currentTimeMillis()+60_000;throw HttpStatusException(r.code) };if(!r.isSuccessful) throw HttpStatusException(r.code)
                    val body=r.body ?: throw IOException("Empty response");require(body.contentLength()<=limit)
                    return withContext(Dispatchers.IO) { val out=java.io.ByteArrayOutputStream();body.byteStream().use { stream->val buffer=ByteArray(16384);while(true) { currentCoroutineContext().ensureActive();val n=stream.read(buffer);if(n<0) break;if(out.size()+n>limit) throw IOException("Response exceeds limit");out.write(buffer,0,n) } };out.toByteArray() }
                }
            }
        };throw IOException("Too many redirects")
    }
    suspend fun get(url: String,limit: Int=8*1024*1024,hosts: Set<String>?=null): String {
        try { return bytes(Request.Builder().url(url).build(),limit,hosts).toString(Charsets.UTF_8) }
        catch(e: IOException) { if(e is HttpStatusException && e.status !in setOf(502,503,504)) throw e;delay(1100);return bytes(Request.Builder().url(url).build(),limit,hosts).toString(Charsets.UTF_8) }
    }
}
