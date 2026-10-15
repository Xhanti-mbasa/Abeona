package za.co.cttrains.core
import kotlinx.serialization.Serializable
/** Fetch time and publication time are distinct; absence never becomes a retrieval timestamp. */
@Serializable data class DataProvenance(val provider: String,val kind: String="scheduled",val fetchedAt: Long,val publishedAt: Long?=null,val cached: Boolean=false,val sourceAvailable: Boolean=true)
