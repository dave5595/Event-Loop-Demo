package org.example.dto

class OrderRejected : AbstractEvent<OrderRejected>() {
    lateinit var order: Order

    companion object {
        fun build(order: Order) = OrderRejected().apply {
            this.order = order
        }
    }
}
