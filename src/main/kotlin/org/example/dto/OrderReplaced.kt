package org.example.dto

class OrderReplaced : AbstractEvent<OrderReplaced>(){
    var oldPrice: Double = 0.0
    var oldQuantity: Double = 0.0
    lateinit var order: Order
    companion object{
        fun build(order: Order, oldPrice: Double, oldQuantity: Double) = OrderReplaced().apply {
            this.order = order
            this.oldPrice = oldPrice
            this.oldQuantity = oldQuantity
        }
    }
}
