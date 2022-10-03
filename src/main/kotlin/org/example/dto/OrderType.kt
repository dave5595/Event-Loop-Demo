package me.oms.order.enum

import com.fasterxml.jackson.annotation.JsonValue

enum class OrderType(/*@get: JsonValue */val type: String) {
    Market("1"),
    Limit("2"),
    StopLoss("3"),
    StopLimit("4"),
    MarketAtBest("Z");

    companion object{
        private val values = values()
        fun valueOfString(type: String): OrderType? {
            return values.firstOrNull { it.type == type }
        }
    }
}
