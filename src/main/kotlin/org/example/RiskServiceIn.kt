package me.oms.vcm.service

import me.oms.vcm.message.AccountCommandListener
import me.oms.vcm.message.OrderEventListener
import me.oms.vcm.message.OrderRequestListener

interface RiskServiceIn: OrderRequestListener, AccountCommandListener, OrderEventListener