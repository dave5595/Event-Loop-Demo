package me.oms.vcm.dto

import com.fasterxml.jackson.databind.ser.Serializers.Base
import me.oms.order.enum.OrderSide
import me.oms.order.enum.OrderType
import net.openhft.chronicle.wire.Base64LongConverter
import net.openhft.chronicle.wire.LongConversion
import software.chronicle.services.api.dto.AbstractEvent

abstract class ExecutionReport : AbstractEvent<ExecutionReport>() {
    abstract var executionType: ExecutionType
    abstract var orderStatus: OrderStatus
    var clOrdId: Long = 0
    @LongConversion(Base64LongConverter::class)
    var exchOrdId: Long = 0

    @LongConversion(Base64LongConverter::class)
    var account: Long = 0

    @LongConversion(Base64LongConverter::class)
    var symbol: Long = 0
    var type: OrderType = OrderType.Limit
    var side: OrderSide = OrderSide.Bid
    var price: Double = 0.0
    var quantity: Double = 0.0

    enum class ExecutionType {
        New, Trade, Rejected, Replaced
    }

    enum class OrderStatus {
        New, Filled, Rejected, Replaced
    }


    companion object {
        fun filled(nos: NewOrderSingle): ExecutionReport {
            return FilledExecutionReport().apply { props(nos) }
        }

        fun rejected(nos: NewOrderSingle): ExecutionReport {
            return RejectedExecutionReport().apply { props(nos) }
        }

        fun new(nos: NewOrderSingle, exchOrdId: CharSequence): ExecutionReport{
            return QueuedExecutionReport().apply {
                props(nos)
                this.exchOrdId = Base64LongConverter.INSTANCE.parse(exchOrdId)
            }
        }

        fun replaced(nos: OrderCancelReplaceRequest): ExecutionReport{
            return ReplacedExecutionReport().apply {
                props(nos)
            }
        }
    }
}

fun ExecutionReport.props(nos: NewOrderSingle) {
    this.clOrdId = nos.clOrdId
    this.account = nos.account
    this.price = nos.price
    this.quantity = nos.quantity
    this.side = nos.side
    this.type = nos.type
    this.symbol = nos.symbol
}

fun ExecutionReport.props(ocrr: OrderCancelReplaceRequest) {
    this.clOrdId = ocrr.clOrdId
    this.exchOrdId = ocrr.exchOrdId
    this.account = ocrr.account
    this.price = ocrr.price
    this.quantity = ocrr.quantity
    this.side = ocrr.side
    this.type = ocrr.type
    this.symbol = ocrr.symbol
}