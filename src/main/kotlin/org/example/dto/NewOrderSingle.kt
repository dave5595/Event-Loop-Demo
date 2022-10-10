package org.example.dto

import net.openhft.chronicle.wire.Base64LongConverter
import net.openhft.chronicle.wire.LongConversion

class NewOrderSingle : AbstractEvent<NewOrderSingle>() {
    var clOrdId: Long = 0

    @LongConversion(Base64LongConverter::class)
    var account: Long = 0

    @LongConversion(Base64LongConverter::class)
    var symbol: Long = 0
    var side: OrderSide = OrderSide.Bid
    var type: OrderType = OrderType.Limit
    var price: Double = 0.0
    var quantity: Double = 0.0

    companion object {
        fun build(order: Order) = NewOrderSingle().apply {
            this.clOrdId = order.id
            this.account = order.accountId
            this.symbol = order.symbol
            this.side = order.side
            this.type = order.type
            this.price = order.price
            this.quantity = order.quantity
        }
    }
}
