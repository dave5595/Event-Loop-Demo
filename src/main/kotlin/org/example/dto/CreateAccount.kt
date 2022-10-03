package me.oms.vcm.dto

import software.chronicle.services.api.dto.AbstractEvent

class CreateAccount(val accountId: Long): AbstractEvent<CreateAccount>()