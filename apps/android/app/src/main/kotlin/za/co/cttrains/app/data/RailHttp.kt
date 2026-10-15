package za.co.cttrains.app.data

import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl.Companion.toHttpUrl
import za.co.cttrains.app.BuildConfig

/** Shared source gate across the home model and the multimodal planner. */
internal object RailHttp {
    private val http = SourceHttp(redirects = false)
    private val gate = Mutex()
    private var lastRequest = 0L
    suspend fun get(url: String): String {
        val host = url.toHttpUrl().host
        val backendHost = BuildConfig.TRANSIT_BASE_URL.takeIf(String::isNotBlank)?.toHttpUrl()?.host
        require(host == "cttrains.co.za" || host == backendHost) { "Unknown rail data source." }
        repeat(2) { attempt ->
            if (host == "cttrains.co.za") gate.withLock {
                val pause = 1100 - (android.os.SystemClock.elapsedRealtime() - lastRequest)
                if (pause > 0) delay(pause)
                lastRequest = android.os.SystemClock.elapsedRealtime()
            }
            try { return http.get(url, 2_000_000, setOf(host)) }
            catch (error: IOException) {
                if (attempt == 1 || (error is HttpStatusException && error.status in 400..499)) throw error
                delay(500)
            }
        }
        error("Unreachable")
    }
}
