package org.example.dto

class OrderQueued : AbstractEvent<OrderQueued>(){
    lateinit var order: Order
    companion object{
        fun build(order: Order) = OrderQueued().apply {
            this.order = order
        }
    }
}