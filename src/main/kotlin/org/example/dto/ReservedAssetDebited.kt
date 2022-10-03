package me.oms.vcm.dto

import net.openhft.chronicle.wire.Base64LongConverter
import net.openhft.chronicle.wire.LongConversion
import software.chronicle.services.api.dto.AbstractEvent

class ReservedAssetDebited : AbstractEvent<ReservedAssetDebited>(){
    @LongConversion(Base64LongConverter::class)
    var symbol: Long = 0
    var accountId: Long = 0
    var debited: Double = 0.0

    companion object{
        fun build(symbol: Long, accountId: Long, amount: Double)=ReservedAssetDebited().apply{
            this.symbol = symbol
            this.accountId = accountId
            this.debited = amount
        }
    }
}
