package me.oms.vcm.service.org.example

import org.example.message.AccountEventListener
import org.example.message.OrderRequestListener

interface RiskServiceOut : OrderRequestListener, AccountEventListener
