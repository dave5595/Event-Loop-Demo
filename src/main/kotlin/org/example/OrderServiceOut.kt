package me.oms.vcm.service

import me.oms.vcm.message.ExchangeOrderListener
import me.oms.vcm.message.OrderEventListener

interface OrderServiceOut: OrderEventListener, ExchangeOrderListener
