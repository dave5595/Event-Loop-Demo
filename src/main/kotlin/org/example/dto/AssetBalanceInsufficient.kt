package me.oms.vcm.dto

import software.chronicle.services.api.dto.AbstractEvent

class AssetBalanceInsufficient(val accountId: Long, val symbol: Long, val balance: Double) :
    AbstractEvent<AssetBalanceInsufficient>()