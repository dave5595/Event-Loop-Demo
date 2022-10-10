package org.example.dto


enum class OrderAction(val action: String) {
    New("new"), Cancel("cancel"), Revise("replace");

    companion object {
        private val values = values()
        fun valueOfString(action: String): OrderAction? {
            return values.firstOrNull { it.action == action }
        }
    }

}
