package org.example.message

import org.example.dto.NewOrderRequest
import org.example.dto.ReplaceOrderRequest


interface OrderRequestListener {
    fun newOrderRequest(request: NewOrderRequest)
}