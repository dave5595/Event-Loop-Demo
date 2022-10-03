package me.oms.vcm.dto

class FilledExecutionReport: ExecutionReport(){
    override var executionType: ExecutionType = ExecutionType.Trade
    override var orderStatus: OrderStatus = OrderStatus.Filled
}

