package me.oms.core.service.wrapper

import me.oms.core.es.SnapshotTriggerDefinition
import me.oms.core.service.Service
import net.openhft.chronicle.bytes.MethodReader
import net.openhft.chronicle.core.io.Closeable
import net.openhft.chronicle.core.threads.EventLoop
import net.openhft.chronicle.core.threads.HandlerPriority
import net.openhft.chronicle.queue.ChronicleQueue
import net.openhft.chronicle.threads.BusyPauser
import net.openhft.chronicle.threads.EventGroup
import net.openhft.chronicle.threads.LongPauser
import net.openhft.chronicle.threads.Pauser
import net.openhft.chronicle.threads.VanillaEventLoop
import java.util.*
import java.util.function.Function

interface ServiceWrapper : Closeable {
    fun inputQueues(): Array<ChronicleQueue>
    fun outputQueue(): ChronicleQueue
    interface MultiBuilder<O, T : Service> {
        val serviceFunctions: List<Function<O, T>>

        fun inputPath(): List<String>
        fun outClass(): Class<O>
        fun addInputPath(inputPath: String): MultiBuilder<O, T>
        fun addInputPaths(inputPaths: List<String>): MultiBuilder<O, T>
        fun outClass(outClass: Class<O>): MultiBuilder<O, T>
        fun outputPath(): String
        fun outputPath(outputPath: String): MultiBuilder<O, T>

        fun addServiceFunction(serviceFunctions: Function<O, T>): MultiBuilder<O, T>
        fun eventLoop(): EventLoop?
        fun createdEventLoop(): Boolean
        fun eventLoop(eventLoop: EventLoop)
        fun priority(): HandlerPriority
        fun priority(priority: HandlerPriority): MultiBuilder<O, T>
        fun inputSourceId(): Int
        fun inputSourceId(inputSourceId: Int): MultiBuilder<O, T>
        fun outputSourceId(): Int
        fun outputSourceId(outputSourceId: Int): MultiBuilder<O, T>
        fun build(): ServiceWrapper
        fun inputQueue(): ChronicleQueue
        fun outputQueue(): ChronicleQueue
        fun outputReader(vararg impls: Any): MethodReader
        fun <T> inputWriter(tClass: Class<T>): T
        fun closeQueues()
    }

    interface SingleBuilder<O, T : Service> {
        val serviceFunction: Function<O, T>
        fun inputPath(): List<String>
        fun outClass(): Class<O>
        fun addInputPath(inputPath: String): SingleBuilder<O, T>
        fun addInputPaths(inputPaths: List<String>): SingleBuilder<O, T>
        fun outClass(outClass: Class<O>): SingleBuilder<O, T>
        fun outputPath(): String
        fun outputPath(outputPath: String): SingleBuilder<O, T>
        fun eventLoop(): EventLoop?
        fun eventLoop(eventLoop: EventLoop)
        fun priority(): HandlerPriority
        fun priority(priority: HandlerPriority): SingleBuilder<O, T>
        fun inputSourceId(): Int
        fun inputSourceId(inputSourceId: Int): SingleBuilder<O, T>
        fun registerSnapshotTriggerDefinitionCfg(snapshotTriggerDefinitionCfg: SnapshotTriggerDefinition.Config?): SingleBuilder<O, T>
        fun registerSnapshotTriggerDefinitionCfg(): SnapshotTriggerDefinition.Config?
        fun periodicUpdateMSInitial(periodicUpdateMSInitial: Long): SingleBuilder<O, T>
        fun periodicUpdateMSInitial(): Long
        fun periodicUpdateMS(periodicUpdateMS: Long, periodicUpdateMSInitial: Long? = 0): SingleBuilder<O, T>
        fun periodicUpdateMS(): Long?
        fun outputSourceId(): Int
        fun outputSourceId(outputSourceId: Int): SingleBuilder<O, T>
        fun build(): ServiceWrapper
        fun inputQueue(): ChronicleQueue
        fun outputQueue(): ChronicleQueue
        fun outputReader(vararg impls: Any): MethodReader
        fun <T> inputWriter(tClass: Class<T>): T
        fun closeQueues()
    }

    companion object {
        fun <O, T : Service> multi(
            inputPath: String,
            outputPath: String,
            outClass: Class<O>,
            serviceFunction: Function<O, T>
        ): MultiBuilder<O, T> {
            return MultiServiceWrapperBuilder.serviceBuilder(inputPath, outputPath, outClass, serviceFunction)
        }

        fun <O, T : Service> multi(
            inputPath: List<String>,
            outputPath: String,
            outClass: Class<O>,
            serviceFunction: Function<O, T>
        ): MultiBuilder<O, T> {
            return MultiServiceWrapperBuilder.serviceBuilder(inputPath, outputPath, outClass, serviceFunction)
        }

        fun <O, T : Service> defaultSingle(
            inputPath: List<String>,
            outputPath: String,
            outClass: Class<O>,
            serviceFunction: Function<O, T>,
            snapshotTriggerDefinitionCfg: SnapshotTriggerDefinition.Config? = null
        ): SingleBuilder<O, T> {

            return SingleServiceWrapperBuilder.serviceBuilder(inputPath, outputPath, outClass, serviceFunction).apply {
                registerSnapshotTriggerDefinitionCfg(snapshotTriggerDefinitionCfg)
            }
        }

        fun <O, T : Service> single(
            inputPath: List<String>,
            outputPath: String,
            outClass: Class<O>,
            serviceFunction: Function<O, T>,
            snapshotTriggerDefinitionCfg: SnapshotTriggerDefinition.Config? = null
        ): SingleBuilder<O, T> {
            return SingleServiceWrapperBuilder.serviceBuilder(inputPath, outputPath, outClass, serviceFunction)
                .apply {
                    registerSnapshotTriggerDefinitionCfg(snapshotTriggerDefinitionCfg)
                }
        }
    }
}
