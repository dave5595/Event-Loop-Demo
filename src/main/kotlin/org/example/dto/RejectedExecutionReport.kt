package me.oms.vcm.dto

import org.example.dto.ExecutionReport

class RejectedExecutionReport : ExecutionReport(){
    override var executionType: ExecutionType = ExecutionType.Rejected
    override var orderStatus: OrderStatus = OrderStatus.Rejected
}

