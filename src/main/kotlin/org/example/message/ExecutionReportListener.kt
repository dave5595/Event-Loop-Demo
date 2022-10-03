package me.oms.vcm.message

import me.oms.vcm.dto.ExecutionReport


interface ExecutionReportListener {
    fun executionReport(report: ExecutionReport)
}