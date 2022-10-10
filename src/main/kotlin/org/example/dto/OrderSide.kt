package org.example.dto

import javax.naming.OperationNotSupportedException

enum class OrderSide(/*@get: JsonValue */val side: String) {
    Bid("1"),
    Ask("2"),
    RegulatedShortSell("5"),
    ProprietaryDayTrading("6"),
    IntradayShortSell("I"),
    PermittedShortSell("V");

    companion object{
        private val values = values()

        fun sideOf(side: String): OrderSide?{
            return values.firstOrNull { it.side == side }
        }
    }

    fun inverse(): OrderSide {
        return when(this){
            Bid -> Ask
            Ask -> Bid
            else ->throw OperationNotSupportedException("inversing operating is not supported for $this")
        }
    }
}
