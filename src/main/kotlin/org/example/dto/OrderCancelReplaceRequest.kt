package me.oms.vcm.dto

import me.oms.order.enum.OrderSide
import me.oms.order.enum.OrderType
import net.openhft.chronicle.wire.Base64LongConverter
import net.openhft.chronicle.wire.LongConversion
import software.chronicle.services.api.dto.AbstractEvent

class OrderCancelReplaceRequest : AbstractEvent<OrderCancelReplaceRequest>() {
    var clOrdId: Long = 0
    var origClOrdId: Long = 0

    @LongConversion(Base64LongConverter::class)
    var exchOrdId: Long = 0

    @LongConversion(Base64LongConverter::class)
    var account: Long = 0

    @LongConversion(Base64LongConverter::class)
    var symbol: Long = 0
    var side: OrderSide = OrderSide.Bid
    var type: OrderType = OrderType.Limit
    var price: Double = 0.0
    var quantity: Double = 0.0

    companion object {
        fun build(order: Order, newPrice: Double, newQuantity: Double) = OrderCancelReplaceRequest().apply {
            this.clOrdId = order.id
            this.origClOrdId = Base64LongConverter.INSTANCE.parse("NONE")
            this.exchOrdId = order.exchOrdId
            this.account = order.accountId
            this.symbol = order.symbol
            this.side = order.side
            this.type = order.type
            this.price = newPrice
            this.quantity = newQuantity

        }
    }
}
