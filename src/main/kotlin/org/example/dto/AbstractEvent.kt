package org.example.dto

import net.openhft.chronicle.wire.LongConversion
import net.openhft.chronicle.wire.NanoTimestampLongConverter
import net.openhft.chronicle.wire.SelfDescribingMarshallable

abstract class AbstractEvent<T : AbstractEvent<T>> : SelfDescribingMarshallable() {
    var eventId: Long = 0

    @LongConversion(NanoTimestampLongConverter::class)
    var eventTime: Long = 0

    fun eventTime(eventTime: Long): T {
        this.eventTime = eventTime
        return this as T
    }
}
