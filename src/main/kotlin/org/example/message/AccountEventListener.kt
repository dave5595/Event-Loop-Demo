package org.example.message

import org.example.dto.*

interface AccountEventListener {
    fun accountCreated(created: AccountCreated)
    fun assetDeposited(deposited: AssetDeposited)
    fun assetReserved(reserved: AssetReserved)
    fun assetUnreserved(reserved: AssetUnreserved)
    fun reservedAssetDebited(reservedDebited: ReservedAssetDebited)
    fun assetBalanceInsufficient(insufficient: AssetBalanceInsufficient)
}
