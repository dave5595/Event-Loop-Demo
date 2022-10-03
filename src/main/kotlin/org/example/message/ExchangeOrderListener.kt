package me.oms.vcm.message

import me.oms.vcm.dto.NewOrderSingle
import me.oms.vcm.dto.OrderCancelReplaceRequest


interface ExchangeOrderListener{
    fun newOrderSingle(nos: NewOrderSingle)

    fun orderCancelReplaceRequest(ocrr: OrderCancelReplaceRequest)

    fun orderCancelRequest(){}
}