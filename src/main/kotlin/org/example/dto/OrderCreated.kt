package me.oms.vcm.dto

import me.oms.order.dto.out.NewOrderSingle
import me.oms.order.enum.*
import me.oms.order.query.OrderEntity
import net.openhft.chronicle.wire.*
import software.chronicle.services.api.dto.AbstractEvent

class OrderCreated : AbstractEvent<OrderCreated>() {
    var orderId: Long = 0
    var extOrderId: Long = 0
    var price: Double = 0.0
    var quantity: Double = 0.0
    @LongConversion(Base64LongConverter::class)
    var symbol: Long = 0
    @LongConversion(Base64LongConverter::class)
    var currency: Long = 0
    var state: OrderState = OrderState.Created
    lateinit var side: OrderSide
    lateinit var type: OrderType
    @LongConversion(Base64LongConverter::class)
    var accountId: Long = 0
}

