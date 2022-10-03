package me.oms.vcm.service

import me.oms.vcm.message.AccountEventListener
import me.oms.vcm.message.OrderRequestListener


interface RiskServiceOut : OrderRequestListener, AccountEventListener
