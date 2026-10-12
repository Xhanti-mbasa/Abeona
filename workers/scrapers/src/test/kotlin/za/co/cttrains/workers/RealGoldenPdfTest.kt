package za.co.cttrains.workers
import org.junit.Test
import org.junit.Assert.*
import org.apache.pdfbox.text.PDFTextStripper
import za.co.cttrains.core.*
class RealGoldenPdfTest {
    @Test fun realGoldenArrowPdfExtractsItsColumnsAndWeekdayRestrictions() {
        val encoded=javaClass.getResourceAsStream("/"+"golden-arrow-pdf.base64")!!.bufferedReader().use { it.readText() }
        val bytes=java.util.Base64.getMimeDecoder().decode(encoded)
        val text=org.apache.pdfbox.Loader.loadPDF(bytes).use { d->PDFTextStripper().apply { sortByPosition=true;pageEnd="\u000c" }.getText(d) }
        val route=BusRouteDocument("004401","Airport Industria","https://www.gabs.co.za/Pdf/Apdf/sample.pdf","2026-09-28",pdf=true)
        val friday=GoldenTimetables.parse(text,route,"Friday",System.currentTimeMillis())
        assertTrue(friday.trips.any { it.calls.first().time=="05:30" && it.calls.last().time=="06:20" })
        assertFalse(friday.trips.any { it.calls.first().time=="05:30" && it.calls.last().time=="06:30" })
        assertTrue(friday.trips.none { t->t.calls.any { it.name=="ROBERT SOBUKWE RD" } })
    }
    @Test fun belharPdfContinuationPagesAndFridayTripsRemainReadable() {
        val encoded=javaClass.getResourceAsStream("/"+"belhar-golden-pdf.base64")!!.bufferedReader().use { it.readText() }
        val bytes=java.util.Base64.getMimeDecoder().decode(encoded)
        val text=org.apache.pdfbox.Loader.loadPDF(bytes).use { d->PDFTextStripper().apply { sortByPosition=true;pageEnd="\u000c" }.getText(d) }
        val document=BusRouteDocument("003001","BELHAR-CAPE TOWN","https://www.gabs.co.za/Pdf/Bpdf/BELHAR___CAPE_TOWN_from_20260928_to_20261004_003001.pdf","2026-09-28","2026-10-04",true)
        val friday=GoldenTimetables.parse(text,document,"Friday",1)
        assertTrue(friday.trips.size>30)
        assertTrue(friday.trips.any { it.calls.first().name=="CAPE TOWN" && it.calls.first().time=="11:00" && it.calls.last().time=="12:25" })
        assertTrue(friday.trips.all { it.calls.zipWithNext().all { (a,b)->a.time<=b.time } })
        assertTrue(friday.trips.none { trip->trip.calls.any { it.name=="ROBERT SOBUKWE RD" } })
    }
}
