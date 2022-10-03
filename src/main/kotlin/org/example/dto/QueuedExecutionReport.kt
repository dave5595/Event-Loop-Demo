package me.oms.vcm.dto

class QueuedExecutionReport: ExecutionReport(){
    override var executionType: ExecutionType = ExecutionType.New
    override var orderStatus: OrderStatus = OrderStatus.New
}