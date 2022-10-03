package me.oms.vcm.dto

import software.chronicle.services.api.dto.AbstractEvent

class OrderFilled : AbstractEvent<OrderFilled>(){
    lateinit var order: Order

    companion object {
        fun build(order: Order) = OrderFilled().apply {
            this.order = order
        }
    }
}
