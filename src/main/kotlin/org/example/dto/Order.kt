package org.example.dto

import net.openhft.chronicle.bytes.MappedUniqueTimeProvider
import net.openhft.chronicle.wire.Base64LongConverter
import net.openhft.chronicle.wire.LongConversion
import net.openhft.chronicle.wire.SelfDescribingMarshallable

class Order : SelfDescribingMarshallable() {
    var id: Long = 0
    var exchOrdId: Long = 0
    var extOrdId: Long = 0
    var price: Double = 0.0
    var quantity: Double = 0.0

    @LongConversion(Base64LongConverter::class)
    var symbol: Long = 0

    @LongConversion(Base64LongConverter::class)
    var currency: Long = 0

    @LongConversion(Base64LongConverter::class)
    var accountId: Long = 0
    lateinit var side: OrderSide
    lateinit var type: OrderType
    lateinit var state: OrderState
    var timestamp = MappedUniqueTimeProvider.INSTANCE.currentTimeMicros()
    val amount get() = price.times(quantity)
    val isWorking get() = state == OrderState.Working || state == OrderState.PartialFilled

    fun fill() = apply {
        timestamp = MappedUniqueTimeProvider.INSTANCE.currentTimeMicros()
        state = OrderState.Filled
    }

    fun reject() = apply {
        timestamp = MappedUniqueTimeProvider.INSTANCE.currentTimeMicros()
        state = OrderState.Rejected
    }

    fun queue(exchOrdId: Long) {
        this.exchOrdId = exchOrdId
        timestamp = MappedUniqueTimeProvider.INSTANCE.currentTimeMicros()
        state = OrderState.Working
    }

    fun replace(price: Double, quantity: Double) {
        this.price = price
        this.quantity = quantity
        timestamp = MappedUniqueTimeProvider.INSTANCE.currentTimeMicros()
        state = OrderState.Working
    }
}