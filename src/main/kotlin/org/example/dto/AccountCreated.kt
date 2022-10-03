package me.oms.vcm.dto

import software.chronicle.services.api.dto.AbstractEvent

class AccountCreated : AbstractEvent<AccountCreated>(){
     var account: Account = Account() 
}