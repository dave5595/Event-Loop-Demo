package me.oms.vcm.service.impl

import me.oms.common.Logger
import me.oms.common.snapshotter.IndexedSnapshot
import me.oms.core.Chronicle.Companion.toLongValue
import me.oms.core.service.EventSourcedService
import me.oms.order.enum.OrderState
import me.oms.vcm.dto.*
import me.oms.vcm.service.OrderServiceIn
import me.oms.vcm.service.OrderServiceOut
import net.openhft.chronicle.core.values.LongValue
import net.openhft.chronicle.map.ChronicleMapBuilder
import net.openhft.chronicle.map.ChronicleMapBuilder.simpleMapOf
import org.agrona.collections.Long2ObjectHashMap
import java.io.File

class OrderService(private val out: OrderServiceOut) : EventSourcedService(), OrderServiceIn {
    private val orders: MutableMap<LongValue, Order>
    private var id = 1L
    private val created = OrderCreated()
    private val nos = NewOrderSingle()
    private val log by Logger()

    init {
        orders = simpleMapOf(LongValue::class.java, Order::class.java)
            .entries(1_000_000)
            .createPersistedTo(File("temp/orders"))
    }

    override fun newOrderRequest(request: NewOrderRequest) {
        val orderId = id++
        val order = newOrder(request, orderId)
        orders[order.id.toLongValue()] = order
        out.orderCreated(created(order))
        out.newOrderSingle(nos.eventTime(request.eventTime()))
    }

    override fun replaceOrderRequest(request: ReplaceOrderRequest) {
        getOrder(request.orderId.toLongValue())?.let { order ->
            if (order.isWorking) {
                out.orderCancelReplaceRequest(OrderCancelReplaceRequest.build(order, request.price, request.quantity))
            } else {
                //order closed! reject request
            }
        }
    }

    override fun executionReport(report: ExecutionReport) {
        getOrder(report.clOrdId.toLongValue())?.let { order ->
            when (report.orderStatus) {
                ExecutionReport.OrderStatus.Filled -> {
                    order.fill()
                    orders.put(report.clOrdId.toLongValue(), order)
                    out.orderFilled(OrderFilled.build(order))
                }
                ExecutionReport.OrderStatus.New -> {
                    order.queue(report.exchOrdId)
                    orders.put(report.clOrdId.toLongValue(), order)
                    out.orderQueued(OrderQueued.build(order))
                }
                ExecutionReport.OrderStatus.Replaced -> {
                    val replaced = OrderReplaced.build(order, order.price, order.quantity)
                    order.replace(report.price, report.quantity)
                    orders.put(report.clOrdId.toLongValue(), order)
                    out.orderReplaced(replaced)
                }
                ExecutionReport.OrderStatus.Rejected -> {
                    order.reject()
                    orders.put(report.clOrdId.toLongValue(), order)
                    out.orderRejected(OrderRejected.build(order))
                }
            }
        }
    }


    private fun created(order: Order): OrderCreated {
        created.orderId = order.id
        created.extOrderId = order.extOrdId
        created.accountId = order.accountId
        created.currency = order.currency
        created.symbol = order.symbol
        created.price = order.price
        created.quantity = order.quantity
        created.side = order.side
        created.type = order.type
        return created
    }

    private fun nos(order: Order): NewOrderSingle {
        nos.clOrdId = order.id
        nos.account = order.accountId
        nos.symbol = order.symbol
        nos.side = order.side
        nos.type = order.type
        nos.price = order.price
        nos.quantity = order.quantity
        return nos
    }

    private fun newOrder(request: NewOrderRequest, id: Long): Order {
        val order = Order()
        order.id = id
        order.extOrdId = request.extOrdId
        order.symbol = request.symbol
        order.accountId = request.accountId
        order.price = request.price
        order.quantity = request.quantity
        order.currency = request.currency
        order.state = OrderState.Created
        order.side = request.side
        order.type = request.type
        return order
    }

    private fun getOrder(id: LongValue): Order? {
        return orders[id]
    }

    override fun getState(): Any {
        return orders
    }

    @Suppress("UNCHECKED_CAST")
    override fun snapshot(snapshot: IndexedSnapshot) {
        (snapshot.state as Map<Long, Order>).forEach { (orderId, order) ->
            orders[orderId.toLongValue()] = order
        }
    }

}