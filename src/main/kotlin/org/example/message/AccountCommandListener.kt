package org.example.message

import org.example.dto.CreateAccount
import org.example.dto.DepositAsset


interface AccountCommandListener{
    fun createAccount(create: CreateAccount)
    fun depositAsset(deposit: DepositAsset)
}
