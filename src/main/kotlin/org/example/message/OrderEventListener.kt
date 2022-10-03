package me.oms.vcm.message

import me.oms.vcm.dto.*

interface OrderEventListener{
    fun orderCreated(created: OrderCreated)
    fun orderFilled(filled: OrderFilled)
    fun orderRejected(rejected: OrderRejected)
    fun orderQueued(queued: OrderQueued)
    fun orderReplaced(replaced: OrderReplaced)

}