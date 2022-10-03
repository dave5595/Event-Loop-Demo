package me.oms.vcm.dto

import net.openhft.chronicle.wire.Base64LongConverter
import net.openhft.chronicle.wire.LongConversion
import software.chronicle.services.api.dto.AbstractEvent

class DepositAsset : AbstractEvent<DepositAsset>() {
    @LongConversion(Base64LongConverter::class)
    var accountId: Long = 0

    @LongConversion(Base64LongConverter::class)
    var symbol: Long = 0
    var amount: Double = 0.0

    companion object {
        fun build(accountId: Long, symbol: CharSequence, amount: Double): DepositAsset {
            return DepositAsset().apply {
                this.accountId = accountId
                this.symbol = Base64LongConverter.INSTANCE.parse(symbol)
                this.amount = amount
            }
        }
    }
}