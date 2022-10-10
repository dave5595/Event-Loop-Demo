package org.example.dto

import net.openhft.chronicle.wire.Base64LongConverter
import net.openhft.chronicle.wire.LongConversion

class NewOrderRequest : AbstractEvent<NewOrderRequest>() {
    var extOrdId: Long = 0
    var price: Double = 0.0
    var quantity: Double = 0.0

    @LongConversion(Base64LongConverter::class)
    var accountId: Long = 0

    @LongConversion(Base64LongConverter::class)
    var symbol: Long = 0

    @LongConversion(Base64LongConverter::class)
    var currency: Long = 0
    var side: OrderSide = OrderSide.Bid
    var type: OrderType = OrderType.Limit
    val amount get() = price.times(quantity)

    companion object {
        private val request = NewOrderRequest()
        val dummy = cimb(price = 2.34, quantity = 100.0)

        fun cimb(price: Double, quantity: Double): NewOrderRequest {
            request.extOrdId = 123
            request.price = price
            request.quantity = quantity
            request.symbol = Base64LongConverter.INSTANCE.parse("CIMB")
            request.accountId = Base64LongConverter.INSTANCE.parse("123456")
            request.currency = Base64LongConverter.INSTANCE.parse("MYR")
            return request
        }
    }
}
