package org.example

import net.openhft.chronicle.bytes.MethodReader
import net.openhft.chronicle.core.io.Closeable
import net.openhft.chronicle.core.threads.EventHandler
import net.openhft.chronicle.core.threads.EventLoop
import net.openhft.chronicle.core.threads.HandlerPriority
import net.openhft.chronicle.core.threads.InvalidEventHandlerException
import net.openhft.chronicle.queue.ChronicleQueue

@Suppress("UNCHECKED_CAST")
internal class EventLoopSingleServiceWrapper<O>(private val builder: ServiceWrapper.Builder<O>) :
    EventHandler, ServiceWrapper {
    private val serviceIn: Array<MethodReader> =
        arrayOfNulls<MethodReader>(builder.inputPath().size) as Array<MethodReader>
    private val priority: HandlerPriority = builder.priority()
    private val inputQueues: Array<ChronicleQueue> =
        arrayOfNulls<ChronicleQueue>(builder.inputPath().size) as Array<ChronicleQueue>
    private val outputQueue: ChronicleQueue = ChronicleQueue.singleBuilder(builder.outputPath())
        .build()

    private val serviceOut: O = outputQueue.acquireAppender()
        .methodWriterBuilder(builder.outClass())
        .build()
    private val serviceImpl: Any = builder.serviceFunction.apply(serviceOut)
    private val serviceName: String =
        serviceImpl::class.simpleName ?: throw IllegalArgumentException("service name is null")

    @Volatile
    private var closed = false
    var eventLoop: EventLoop? = builder.eventLoop()

    init {
        val paths = builder.inputPath()

        for (i in paths.indices) {
            inputQueues[i] = ChronicleQueue.singleBuilder(paths[i])
                .build()
            serviceIn[i] = inputQueues[i].createTailer(serviceName).methodReader(serviceImpl)
        }
        eventLoop!!.addHandler(this)
        startWrapper()
    }

    override fun inputQueues(): Array<ChronicleQueue> {
        return inputQueues
    }

    override fun outputQueue(): ChronicleQueue {
        return outputQueue
    }

    @Throws(InvalidEventHandlerException::class)
    override fun action(): Boolean {
        if (isClosed) {
            Closeable.closeQuietly(serviceImpl)
            Closeable.closeQuietly(serviceIn as Array<Any>)
            Closeable.closeQuietly(outputQueue)
            Closeable.closeQuietly(inputQueues as Array<Any>)
            throw InvalidEventHandlerException.reusable()
        }
        var busy = false
        for (reader in serviceIn) {
            busy = busy or reader.readOne()
        }
        return busy
    }


    override fun priority(): HandlerPriority {
        return priority
    }

    private fun startWrapper() {
        try {
            closed = false
            if (eventLoop == null) {
                eventLoop = builder.eventLoop()
            }
            if (!eventLoop!!.isAlive) {
                eventLoop!!.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun close() {
        closed = true
        val eventLoop = eventLoop
        eventLoop?.unpause()
        this.eventLoop = null
        eventLoop?.close()
    }

    override fun isClosed(): Boolean {
        return closed
    }
}
