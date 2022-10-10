package org.example.dto

import net.openhft.chronicle.wire.Base64LongConverter
import net.openhft.chronicle.wire.LongConversion

class AssetReserved : AbstractEvent<AssetReserved>() {
    @LongConversion(Base64LongConverter::class)
    var accountId: Long = 0

    @LongConversion(Base64LongConverter::class)
    var symbol: Long = 0
    var amount: Double = 0.0

    companion object {
        fun build(accountId: Long, symbol: Long, amount: Double) = AssetReserved().apply {
            this.accountId = accountId
            this.symbol = symbol
            this.amount = amount
        }

    }
}