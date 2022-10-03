package me.oms.vcm.dto

import me.oms.risk.AccountAggregate
import net.openhft.chronicle.wire.Base64LongConverter
import net.openhft.chronicle.wire.LongConversion
import net.openhft.chronicle.wire.SelfDescribingMarshallable

class Account(var status: Status = Status.Active, var assets: MutableMap<Long, Asset> = HashMap()) : SelfDescribingMarshallable() {
    var id: Long = 0

    companion object {
        fun new(id: Long) = Account().apply { this.id = id }
    }

    fun suspend() = apply {
        status = Status.Suspended
    }

    fun unsuspend() = apply {
        status = Status.Active
    }

    fun getAsset(symbol: Long): Asset? {
        return assets[symbol]
    }

    fun addAsset(asset: Asset) = apply {
        assets[asset.symbol] = asset
    }

    fun transferAsset(toAccount: AccountAggregate, asset: me.oms.risk.Asset) {
        toAccount.receiveAsset(asset)
    }

    enum class Status {
        Active, Suspended, Blacklisted
    }

    class Asset : SelfDescribingMarshallable() {
        @LongConversion(Base64LongConverter::class)
        var symbol: Long = 0
        var freeBalance: Double = 0.0
        var reserved: Double = 0.0
        val balance: Double get() = freeBalance + reserved

        companion object{
            fun create(symbol: Long, balance: Double) = Asset().apply{
                this.symbol = symbol
                deposit(balance)
            }
        }

        fun reserve(amount: Double): Boolean {
            reserved = me.oms.vcm.FormatUtil.threeDecimalPoints(reserved + amount)
            freeBalance = me.oms.vcm.FormatUtil.threeDecimalPoints(freeBalance - amount)
            return freeBalance.compareTo(0.0) >= 0
        }

        fun unreserve(amount: Double) = apply {
            reserved = me.oms.vcm.FormatUtil.threeDecimalPoints(reserved - amount)
            freeBalance = me.oms.vcm.FormatUtil.threeDecimalPoints(freeBalance + amount)
        }

        fun refund(amount: Double) {
            freeBalance = me.oms.vcm.FormatUtil.threeDecimalPoints(freeBalance + amount)
            reserved = me.oms.vcm.FormatUtil.threeDecimalPoints(reserved - amount)
        }


        fun debitReserved(amount: Double){
            reserved = me.oms.vcm.FormatUtil.threeDecimalPoints(reserved - amount)
        }

        fun withdraw(amount: Double) = apply {
            freeBalance = me.oms.vcm.FormatUtil.threeDecimalPoints(freeBalance - amount)
        }

        fun deposit(amount: Double) = apply {
            freeBalance = me.oms.vcm.FormatUtil.threeDecimalPoints(freeBalance + amount)
        }
    }
}
