package me.oms.core.service.wrapper

import me.oms.core.EventLoopScheduler
import me.oms.core.es.EventSourcer
import me.oms.core.es.SnapshotTriggerDefinition
import me.oms.core.service.EventSourcedService
import me.oms.core.service.PeriodicUpdateSource
import me.oms.core.service.Service
import me.oms.common.Logger
import me.oms.core.es.IndexedSnapshotProvider
import net.openhft.chronicle.bytes.MethodReader
import net.openhft.chronicle.core.io.Closeable
import net.openhft.chronicle.core.threads.EventHandler
import net.openhft.chronicle.core.threads.EventLoop
import net.openhft.chronicle.core.threads.HandlerPriority
import net.openhft.chronicle.core.threads.InvalidEventHandlerException
import net.openhft.chronicle.queue.ChronicleQueue
import net.openhft.chronicle.wire.ServicesTimestampLongConverter

@Suppress("UNCHECKED_CAST")
internal class EventLoopSingleServiceWrapper<O, T : Service>(private val builder: ServiceWrapper.SingleBuilder<O, T>) :
    EventHandler, ServiceWrapper {
    private val log by Logger()
    private val serviceIn: Array<MethodReader> =
        arrayOfNulls<MethodReader>(builder.inputPath().size) as Array<MethodReader>
    private val priority: HandlerPriority = builder.priority()
    private val inputQueues: Array<ChronicleQueue> =
        arrayOfNulls<ChronicleQueue>(builder.inputPath().size) as Array<ChronicleQueue>
    private val outputQueue: ChronicleQueue = ChronicleQueue.singleBuilder(builder.outputPath())
        .sourceId(builder.outputSourceId())
        .checkInterrupts(false)
        .build()

    private val serviceOut: O = outputQueue.acquireAppender()
        .methodWriterBuilder(builder.outClass())
        .build()
    val serviceImpl: T = builder.serviceFunction.apply(serviceOut)
    private val serviceName: String =
        serviceImpl::class.simpleName ?: throw IllegalArgumentException("service name is null")
    private val periodicUpdateMS = builder.periodicUpdateMS()
    private var periodicUpdateSource: PeriodicUpdateSource? = null
    private val indexedSnapshotProvider: IndexedSnapshotProvider<EventSourcedService> =
        IndexedSnapshotProvider(outputQueue, serviceImpl as EventSourcedService)
    private var eventSourcer: EventSourcer<EventSourcedService>? = null

    @Volatile
    private var closed = false
    var eventLoop: EventLoop? = builder.eventLoop()

    init {
        eventLoop ?: log.warn("event loop is null for $serviceImpl")
        val paths = builder.inputPath()

        for (i in paths.indices) {
            inputQueues[i] = ChronicleQueue.singleBuilder(paths[i])
                .sourceId(builder.inputSourceId())
                .build()
            serviceIn[i] = inputQueues[i].createTailer(serviceName).methodReader(serviceImpl)
        }
        addEventSourcer()
        addPeriodicUpdateInvokingHandler()
        eventLoop!!.addHandler(this)
        startWrapper()
    }

    private fun addEventSourcer() {
        val snapshotTriggerDefinitionCfg = builder.registerSnapshotTriggerDefinitionCfg()
        //enable snapshotting capabilities if snapshotTriggerDefinitionCfg is defined in builder
        if (snapshotTriggerDefinitionCfg != null) {
            eventSourcer = buildEventSourcer(snapshotTriggerDefinitionCfg)
        }
    }

    private fun buildEventSourcer(snapshotTriggerDefinitionCfg: SnapshotTriggerDefinition.Config): EventSourcer<EventSourcedService> {
        return snapshotTriggerDefinitionCfg.buildEventSourcer(indexedSnapshotProvider, eventLoop!!)
    }

    private fun addPeriodicUpdateInvokingHandler() {
        if (periodicUpdateMS != null) {
            periodicUpdateSource = inputQueues
                .first()
                .acquireAppender()
                .methodWriter(PeriodicUpdateSource::class.java)
            EventLoopScheduler(eventLoop!!).scheduleAtFixedRate(EventHandler {
                periodicUpdateSource!!.periodicUpdate(ServicesTimestampLongConverter.currentTime())
                true
            }, builder.periodicUpdateMSInitial(), periodicUpdateMS, HandlerPriority.DAEMON)
        }
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
            scheduleSnapshot()
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

    private fun scheduleSnapshot() {
        eventSourcer?.scheduleSnapshot()
    }

    override fun priority(): HandlerPriority {
        return priority
    }

    private fun startWrapper() {
        try {
            closed = false
            eventSourcer?.recoverState()
            if (eventLoop == null) {
                eventLoop = builder.eventLoop()
            }
            if (!eventLoop!!.isAlive) {
                eventLoop!!.start()
            }
        } catch (e: Exception) {
            log.error("exception starting $serviceName wrapper: $e")
            e.printStackTrace()
        }
    }

    override fun close() {
        log.info("$serviceName wrapper closing")
        closed = true

        val eventLoop = eventLoop
        eventLoop?.unpause()
        this.eventLoop = null
        eventLoop?.close()
    }

    override fun isClosed(): Boolean {
        return closed
    }

    /*override fun snapshotState(service: Service) {
        try {
            log.info("snapshotting state")
            val index = outputQueue.lastIndex()
            val state = service.getState()
            val snapshot = IndexedSnapshot(state, index)
            log.info("saved snapshot state: $snapshot")
            stateSnapshotter.snapshot(snapshot)
        } catch (e: IllegalStateException) {
            log.warn("state snapshot encountered an issue: $e")
        }
    }

    override fun recoverState(service: Service) {
        snapshotTailer.readDocument {
            //unmarshall IndexedSnapshot
            val indexedSnapshot = it.read("snapshot").`object`(IndexedSnapshot::class.java)
            //if present
            indexedSnapshot?.let { snapshot ->
                log.info("snapshot found during state recovery: $snapshot")
                //apply snapshot to service
                service.snapshot(snapshot)
                //move tailer to snapshot index
                outputTailer.moveToIndex(snapshot.index + 1)
            }
            val outputReader = outputTailer.methodReader(serviceImpl)
            //read remaining output events if any
            while (true) {
                if (!outputReader.readOne()) {
                    return@readDocument
                }
            }
        }
    }*/
}
