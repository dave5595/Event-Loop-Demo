package me.oms.vcm.dto

import software.chronicle.services.api.dto.AbstractEvent

class OrderQueued : AbstractEvent<OrderQueued>(){
    lateinit var order: Order
    companion object{
        fun build(order: Order) = OrderQueued().apply {
            this.order = order
        }
    }
}