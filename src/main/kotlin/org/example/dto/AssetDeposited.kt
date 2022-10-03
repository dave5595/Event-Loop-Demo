package me.oms.vcm.dto

import net.openhft.chronicle.wire.Base64LongConverter
import net.openhft.chronicle.wire.LongConversion
import software.chronicle.services.api.dto.AbstractEvent

class AssetDeposited : AbstractEvent<AssetDeposited>(){
    @LongConversion(Base64LongConverter::class)
    var accountId: Long = 0
    @LongConversion(Base64LongConverter::class)
    var symbol: Long = 0
    var deposited: Double = 0.0
    lateinit var asset: Account.Asset

    companion object{
        fun build(accountId: Long, asset: Account.Asset, deposited: Double ): AssetDeposited{
            return AssetDeposited().apply {
                this.accountId = accountId
                this.symbol = asset.symbol
                this.deposited = deposited
                this.asset = asset
            }
        }
    }
}