package me.oms.vcm.dto

import me.oms.vcm.FormatUtil
import net.openhft.chronicle.wire.Base64LongConverter
import net.openhft.chronicle.wire.LongConversion
import software.chronicle.services.api.dto.AbstractEvent

class ReplaceOrderRequest : AbstractEvent<ReplaceOrderRequest>() {
    var orderId: Long = 0
    var extOrdId: Long = 0
    var price: Double = 0.0
    var quantity: Double = 0.0
    @LongConversion(Base64LongConverter::class)
    var accountId: Long = 0
    val amount get() = me.oms.vcm.FormatUtil.threeDecimalPoints(price.times(quantity))

    companion object {
        private val request = ReplaceOrderRequest()

        fun cimb(orderId: Long, price: Double, quantity: Double, accountId: Long): ReplaceOrderRequest {
            request.orderId = orderId
            request.extOrdId = 123
            request.price = price
            request.quantity = quantity
            request.accountId = accountId
            return request
        }
    }
}
