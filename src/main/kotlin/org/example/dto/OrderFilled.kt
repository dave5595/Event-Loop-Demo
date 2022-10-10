package org.example.dto

class OrderFilled : AbstractEvent<OrderFilled>(){
    lateinit var order: Order

    companion object {
        fun build(order: Order) = OrderFilled().apply {
            this.order = order
        }
    }
}
