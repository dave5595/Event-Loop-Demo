package org.example

import net.openhft.chronicle.core.values.LongValue
import net.openhft.chronicle.values.Values

fun Long.toLongValue(): LongValue {
    return Values.newHeapInstance(LongValue::class.java).also {
        it.value = this
    }
}