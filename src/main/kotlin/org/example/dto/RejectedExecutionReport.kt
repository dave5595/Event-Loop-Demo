package me.oms.vcm.dto

class RejectedExecutionReport : ExecutionReport(){
    override var executionType: ExecutionType = ExecutionType.Rejected
    override var orderStatus: OrderStatus = OrderStatus.Rejected
}

