package org.example.message

import org.example.dto.*

interface OrderEventListener{
    fun orderCreated(created: OrderCreated)
    fun orderFilled(filled: OrderFilled)
    fun orderRejected(rejected: OrderRejected)
    fun orderQueued(queued: OrderQueued)
    fun orderReplaced(replaced: OrderReplaced)

}