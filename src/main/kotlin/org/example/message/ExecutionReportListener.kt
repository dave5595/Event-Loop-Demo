package org.example.message

import org.example.dto.ExecutionReport


interface ExecutionReportListener {
    fun executionReport(report: ExecutionReport)
}