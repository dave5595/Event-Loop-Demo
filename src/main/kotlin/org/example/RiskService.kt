package org.example

import me.oms.vcm.dto.*
import me.oms.vcm.service.org.example.RiskServiceIn
import me.oms.vcm.service.org.example.RiskServiceOut
import net.openhft.chronicle.core.values.LongValue
import net.openhft.chronicle.map.ChronicleMapBuilder
import org.example.dto.*
import java.io.File

class RiskService(private val out: RiskServiceOut) : RiskServiceIn {
    private val accounts: MutableMap<Long, Account> = HashMap()

    override fun newOrderRequest(request: NewOrderRequest) {
          out.newOrderRequest(request)
    }
}



