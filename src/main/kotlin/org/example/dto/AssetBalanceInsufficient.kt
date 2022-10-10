package org.example.dto


class AssetBalanceInsufficient(val accountId: Long, val symbol: Long, val balance: Double) :
    AbstractEvent<AssetBalanceInsufficient>()