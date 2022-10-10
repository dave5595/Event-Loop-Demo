package org.example.message

import org.example.dto.NewOrderSingle
import org.example.dto.OrderCancelReplaceRequest


interface ExchangeOrderListener{
    fun newOrderSingle(nos: NewOrderSingle)

    fun orderCancelReplaceRequest(ocrr: OrderCancelReplaceRequest)

    fun orderCancelRequest(){}
}