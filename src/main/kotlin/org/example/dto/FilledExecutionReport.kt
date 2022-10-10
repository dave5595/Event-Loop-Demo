package org.example.dto

class FilledExecutionReport: ExecutionReport(){
    override var executionType: ExecutionType = ExecutionType.Trade
    override var orderStatus: OrderStatus = OrderStatus.Filled
}

