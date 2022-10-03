package me.oms.vcm.dto

import software.chronicle.services.api.dto.AbstractEvent

class OrderRejected : AbstractEvent<OrderRejected>() {
    lateinit var order: Order

    companion object {
        fun build(order: Order) = OrderRejected().apply {
            this.order = order
        }
    }
}
