package me.oms.vcm.message

import me.oms.vcm.dto.*

interface AccountEventListener {
    fun accountCreated(created: AccountCreated)
    fun assetDeposited(deposited: AssetDeposited)
    fun assetReserved(reserved: AssetReserved)
    fun assetUnreserved(reserved: AssetUnreserved)
    fun reservedAssetDebited(reservedDebited: ReservedAssetDebited)
    fun assetBalanceInsufficient(insufficient: AssetBalanceInsufficient)
}
