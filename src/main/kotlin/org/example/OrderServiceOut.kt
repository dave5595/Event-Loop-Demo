package org.example

import org.example.message.ExchangeOrderListener
import org.example.message.OrderEventListener

interface OrderServiceOut: OrderEventListener, ExchangeOrderListener
