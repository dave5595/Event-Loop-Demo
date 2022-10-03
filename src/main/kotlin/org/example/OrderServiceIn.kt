package me.oms.vcm.service

import me.oms.vcm.message.ExecutionReportListener
import me.oms.vcm.message.OrderRequestListener

interface OrderServiceIn : OrderRequestListener, ExecutionReportListener



