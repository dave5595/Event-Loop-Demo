package me.oms.vcm.dto

import net.openhft.chronicle.wire.Base64LongConverter
import net.openhft.chronicle.wire.LongConversion
import software.chronicle.services.api.dto.AbstractEvent

class AssetUnreserved : AbstractEvent<AssetUnreserved>(){
    @LongConversion(Base64LongConverter::class)
    var accountId: Long = 0
    @LongConversion(Base64LongConverter::class)
    var symbol: Long = 0
    var amount: Double = 0.0

    companion object {
        fun build(accountId: Long, symbol: Long, amount: Double) = AssetUnreserved().apply {
            this.accountId = accountId
            this.symbol = symbol
            this.amount = amount
        }

    }
}
