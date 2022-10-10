package org.example

import net.openhft.chronicle.bytes.MethodReader
import net.openhft.chronicle.core.io.Closeable
import net.openhft.chronicle.core.threads.EventLoop
import net.openhft.chronicle.core.threads.HandlerPriority
import net.openhft.chronicle.queue.ChronicleQueue
import java.util.function.Function

interface ServiceWrapper : Closeable {
    fun inputQueues(): Array<ChronicleQueue>
    fun outputQueue(): ChronicleQueue

    interface Builder<O> {
        val serviceFunction: Function<O, Any>
        fun inputPath(): List<String>
        fun outClass(): Class<O>
        fun addInputPath(inputPath: String): Builder<O>
        fun addInputPaths(inputPaths: List<String>): Builder<O>
        fun outClass(outClass: Class<O>): Builder<O>
        fun outputPath(): String
        fun outputPath(outputPath: String): Builder<O>
        fun eventLoop(): EventLoop?
        fun eventLoop(eventLoop: EventLoop)
        fun priority(): HandlerPriority
        fun priority(priority: HandlerPriority): Builder<O>
        fun inputSourceId(): Int
        fun inputSourceId(inputSourceId: Int): Builder<O>

        fun outputSourceId(): Int
        fun outputSourceId(outputSourceId: Int): Builder<O>
        fun build(): ServiceWrapper
        fun inputQueue(): ChronicleQueue
        fun outputQueue(): ChronicleQueue
        fun outputReader(vararg impls: Any): MethodReader
        fun <T> inputWriter(tClass: Class<T>): T
        fun closeQueues()
    }

    companion object {

        fun <O> builder(
            inputPath: List<String>,
            outputPath: String,
            outClass: Class<O>,
            serviceFunction: Function<O, Any>
        ): Builder<O> {
            return SingleServiceWrapperBuilder.serviceBuilder(inputPath, outputPath, outClass, serviceFunction)
        }
    }
}
