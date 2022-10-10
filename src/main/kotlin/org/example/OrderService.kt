package org.example

import me.oms.vcm.dto.*
import net.openhft.chronicle.core.values.LongValue
import net.openhft.chronicle.map.ChronicleMapBuilder.simpleMapOf
import net.openhft.chronicle.values.Values
import org.example.dto.*
import java.io.File

class OrderService(private val out: OrderServiceOut) : OrderServiceIn {
    private val orders: MutableMap<LongValue, NewOrderRequest>
    private val nos = NewOrderSingle()

    init {
        orders = simpleMapOf(LongValue::class.java, NewOrderRequest::class.java)
            .entries(1_000_000)
            .createPersistedTo(File("temp/orders"))
    }

    override fun newOrderRequest(request: NewOrderRequest) {
        orders[request.extOrdId.toLongValue()] = request
        out.newOrderSingle(nos.eventTime(request.eventTime))
    }

}
