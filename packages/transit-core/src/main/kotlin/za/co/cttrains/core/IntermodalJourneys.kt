package za.co.cttrains.core

/** Join only catchable, fully timed stages. Each stage already includes its mapped access walk and boarding buffer. */
object IntermodalJourneys {
    fun connect(first: TripEstimate,second: TripEstimate,requestedAt: Long): TripEstimate? {
        val changeAt=first.arrivalAt ?: return null
        val arrival=second.arrivalAt ?: return null
        if(first.canCatch!=true || second.canCatch!=true || first.walkingMinutes==null || second.walkingMinutes==null) return null
        if(first.departureAt<requestedAt || changeAt<first.departureAt || second.departureAt<changeAt || arrival<second.departureAt) return null
        val minutes=kotlin.math.ceil((arrival-requestedAt)/60_000.0).toInt()
        if(minutes !in 1..480) return null
        return TripEstimate(first.departureAt,arrival,minutes,first.walkingMinutes+second.walkingMinutes,
            if(first.waitingMinutes!=null && second.waitingMinutes!=null) first.waitingMinutes+second.waitingMinutes else null,true)
    }
}
