package org.example

import net.openhft.chronicle.bytes.MethodReader
import net.openhft.chronicle.queue.ChronicleQueue
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder
import net.openhft.chronicle.threads.BusyPauser
import net.openhft.chronicle.threads.Pauser
import java.util.function.Function
import kotlin.concurrent.thread

object Service {
    fun <T> create(
        serviceName: String,
        inputPath: String,
        outputPath: String,
        outClass: Class<T>,
        serviceFunction: Function<T, Any>,
        pauser: Pauser = BusyPauser.INSTANCE
    ) = Runnable {
        SingleChronicleQueueBuilder.binary(inputPath).build().use { `in` ->
            SingleChronicleQueueBuilder.binary(outputPath).build().use { out ->
                val serviceOut = out.acquireAppender().methodWriter(outClass)
                val service = serviceFunction.apply(serviceOut)
                val reader = `in`.createTailer(serviceName).methodReader(service)
                while (!Thread.currentThread().isInterrupted) {
                    if (reader.readOne()) pauser.reset()
                    else pauser.pause()
                }
            }
        }
    }

    fun <T> create(
        serviceName: String,
        inputPaths: List<String>,
        outputPath: String,
        outClass: Class<T>,
        serviceFunction: Function<T, Any>,
        pauser: Pauser = BusyPauser.INSTANCE
    ) = Runnable {
        val inputQueues: Array<ChronicleQueue> =
            arrayOfNulls<ChronicleQueue>(inputPaths.size) as Array<ChronicleQueue>
        val serviceIn: Array<MethodReader> = arrayOfNulls<MethodReader>(inputPaths.size) as Array<MethodReader>
        val outputQueue = ChronicleQueue.single(outputPath)
        val serviceOut = outputQueue.acquireAppender().methodWriter(outClass)
        val serviceImpl = serviceFunction.apply(serviceOut)
        for (idx in inputPaths.indices) {
            inputQueues[idx] = ChronicleQueue.singleBuilder(inputPaths[idx])
                .build()
            serviceIn[idx] = inputQueues[idx].createTailer(serviceName).methodReader(serviceImpl)
        }

        while (!Thread.currentThread().isInterrupted) {
            var busy = false
            for (input in serviceIn) {
                busy = busy or input.readOne()
            }
            if (busy) pauser.reset()
            else pauser.pause()
        }
    }

}

object Runner {
    fun withThread(runnable: Runnable, start: Boolean = true, isDaemon: Boolean = true): Thread {
        return thread(isDaemon = isDaemon, start = start) {
            runnable.run()
        }
    }
}