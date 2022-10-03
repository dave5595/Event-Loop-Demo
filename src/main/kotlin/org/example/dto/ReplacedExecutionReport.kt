package me.oms.vcm.dto

class ReplacedExecutionReport : ExecutionReport(){
    override var executionType: ExecutionType = ExecutionType.Replaced
    override var orderStatus: OrderStatus = OrderStatus.Replaced
}
