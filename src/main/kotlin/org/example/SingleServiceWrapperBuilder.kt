package org.example

import net.openhft.chronicle.bytes.MethodReader
import net.openhft.chronicle.core.io.Closeable
import net.openhft.chronicle.core.threads.EventLoop
import net.openhft.chronicle.core.threads.HandlerPriority
import net.openhft.chronicle.queue.ChronicleQueue
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder
import net.openhft.chronicle.threads.BusyPauser
import net.openhft.chronicle.threads.EventGroup
import net.openhft.chronicle.threads.Pauser
import java.util.*
import kotlin.collections.ArrayList
import java.util.function.Function

class SingleServiceWrapperBuilder<O> : ServiceWrapper.Builder<O> {
    private val inputPaths: MutableList<String> = ArrayList()

    override lateinit var serviceFunction: Function<O, Any>
    private lateinit var outputPath: String
    private lateinit var outClass: Class<O>
    private var eventLoop: EventLoop? = null
    private var priority = HandlerPriority.MEDIUM
    private var createdEventLoop = false
    private var inputSourceId = 0
    private var outputSourceId = 0
    private val queues: MutableList<ChronicleQueue> = ArrayList()

    override fun inputPath(): List<String> {
        return inputPaths
    }

    override fun addInputPath(inputPath: String): ServiceWrapper.Builder<O> {
        inputPaths.add(inputPath)
        return this
    }

    override fun addInputPaths(inputPaths: List<String>): ServiceWrapper.Builder<O> {
        this.inputPaths.addAll(inputPaths)
        return this
    }

    override fun outClass(): Class<O> {
        return outClass
    }

    override fun outClass(outClass: Class<O>): ServiceWrapper.Builder<O> {
        this.outClass = outClass
        return this
    }

    override fun outputPath(): String {
        return outputPath
    }

    override fun outputPath(outputPath: String): ServiceWrapper.Builder<O> {
        this.outputPath = outputPath
        return this
    }

    override fun eventLoop(): EventLoop? {
        return eventLoop
    }

    override fun eventLoop(eventLoop: EventLoop) {
        this.eventLoop = eventLoop
    }

    override fun priority(): HandlerPriority {
        return priority
    }

    override fun priority(priority: HandlerPriority): ServiceWrapper.Builder<O> {
        this.priority = priority
        return this
    }

    override fun inputSourceId(): Int {
        return inputSourceId
    }

    override fun inputSourceId(inputSourceId: Int): ServiceWrapper.Builder<O> {
        this.inputSourceId = inputSourceId
        return this
    }


    override fun outputSourceId(): Int {
        return outputSourceId
    }

    override fun outputSourceId(outputSourceId: Int): ServiceWrapper.Builder<O> {
        this.outputSourceId = outputSourceId
        return this
    }

    override fun build(): ServiceWrapper {
        createdEventLoop = true
        eventLoop = EventGroup(
            true,// Event group threads are created as daemons, see java.lang.Thread#daemon
            BusyPauser.INSTANCE,// Busy pauser - event group threads spin instead of sleeping if there's no work
            null,// This setting is not relevant - replication is not used
            "any",// Binding for further pinning thread to CPU via AffinityLock.acquireLock,
            // see https://github.com/OpenHFT/Java-Thread-Affinity#question-how-to-use-a-configuration-file-to-set-the-cpuid
            null, // This setting is not relevant - replication is not used
            "eg/",// Name prefix for event group threads
            0, // This setting is not relevant - concurrent handlers are not used
            null, // This setting is not relevant - concurrent handlers are not used
            { BusyPauser.INSTANCE },  // This setting is not relevant - concurrent handlers are not used
            // Minimum set of handlers' priorities for running the scenario. MEDIUM for the main working thread,
            // BLOCKING for TCP accept / connect, MONITOR/TIMER for misc monitoring activities
            EnumSet.of(
                HandlerPriority.DAEMON,
                HandlerPriority.MONITOR,
                HandlerPriority.HIGH
            ),
            { Pauser.balanced() }
        )
        return EventLoopSingleServiceWrapper(this)
    }

    override fun inputQueue(): ChronicleQueue {
        val build = SingleChronicleQueueBuilder
            .binary(inputPaths[0])
            .sourceId(inputSourceId())
            .checkInterrupts(false)
            .build()
        queues.add(build)
        return build
    }

    override fun outputQueue(): ChronicleQueue {
        val build = SingleChronicleQueueBuilder
            .binary(outputPath)
            .sourceId(outputSourceId())
            .checkInterrupts(false)
            .build()
        queues.add(build)
        return build
    }

    override fun outputReader(vararg impls: Any): MethodReader {
        val queue = outputQueue()
        val reader = queue.createTailer().methodReader(*impls)
        reader.closeIn(true)
        return reader
    }

    override fun <T> inputWriter(tClass: Class<T>): T {
        val queue = inputQueue()
        return queue.acquireAppender()
            .methodWriterBuilder(tClass)
            .onClose(queue)
            .get()
    }

    override fun closeQueues() {
        Closeable.closeQuietly(queues)
    }

    companion object {

        fun <O> serviceBuilder(
            inputPath: String,
            outputPath: String,
            outClass: Class<O>,
            serviceFunction: Function<O, Any>
        ): ServiceWrapper.Builder<O> {
            val swb: SingleServiceWrapperBuilder<O> = SingleServiceWrapperBuilder()
            swb.addInputPath(inputPath)
            swb.outputPath = outputPath
            swb.outClass = outClass
            swb.serviceFunction = serviceFunction
            return swb
        }

        fun <O> serviceBuilder(
            inputPaths: List<String>,
            outputPath: String,
            outClass: Class<O>,
            serviceFunction: Function<O, Any>
        ): ServiceWrapper.Builder<O> {
            val swb: SingleServiceWrapperBuilder<O> = SingleServiceWrapperBuilder()
            swb.addInputPaths(inputPaths)
            swb.outputPath = outputPath
            swb.outClass = outClass
            swb.serviceFunction = serviceFunction
            return swb
        }
    }
}