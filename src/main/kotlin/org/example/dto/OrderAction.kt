package me.oms.order.enum

import com.fasterxml.jackson.annotation.JsonValue

enum class OrderAction(@get:JsonValue val action: String) {
    New("new"), Cancel("cancel"), Revise("replace");

    companion object {
        private val values = values()
        fun valueOfString(action: String): OrderAction? {
            return values.firstOrNull { it.action == action }
        }
    }

}
