package za.co.cttrains.core
import org.junit.Assert.*
import org.junit.Test

class SavedPlaceRetentionTest {
    @Test fun editingAnAddressDoesNotSilentlyEvictOlderPersonalPlaces() {
        val home=MarkedSpot("home-kept","Home",null,GeoPoint(-33.9,18.4),"Home","Old address",createdAt=1,preferredStation="Unibell")
        val others=(1..150).map { MarkedSpot("place-$it","Place $it",null,GeoPoint(-33.9,18.4),"Custom","",createdAt=it.toLong()) }
        val result=SavedAddresses.upsert(listOf(home)+others,home.copy(point=GeoPoint(-33.91,18.41),note="New address"))
        assertEquals(151,result.size);assertEquals(others.toSet(),result.filter { it.id!=home.id }.toSet())
        assertEquals("New address",result.single { it.id==home.id }.note);assertEquals("Unibell",result.single { it.id==home.id }.preferredStation)
        val added=SavedAddresses.upsert(result,MarkedSpot("new","Evening class",null,GeoPoint(-33.9,18.4),"Custom","",createdAt=200))
        assertEquals(152,added.size);assertTrue(added.containsAll(result))
    }
}
