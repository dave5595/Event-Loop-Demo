package me.oms.vcm.message

import me.oms.vcm.dto.CreateAccount
import me.oms.vcm.dto.DepositAsset


interface AccountCommandListener{
    fun createAccount(create: CreateAccount)
    fun depositAsset(deposit: DepositAsset)
}
