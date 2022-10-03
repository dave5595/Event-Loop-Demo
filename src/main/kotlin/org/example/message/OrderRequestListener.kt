package me.oms.vcm.message

import me.oms.vcm.dto.NewOrderRequest
import me.oms.vcm.dto.ReplaceOrderRequest


interface OrderRequestListener {
    fun newOrderRequest(request: NewOrderRequest)
    fun replaceOrderRequest(request: ReplaceOrderRequest)
}