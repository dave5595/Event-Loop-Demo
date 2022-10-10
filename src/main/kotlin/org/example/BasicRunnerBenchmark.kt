package org.example

import me.oms.vcm.service.org.example.RiskServiceOut
import net.openhft.chronicle.core.io.IOTools
import net.openhft.chronicle.jlbh.JLBH
import net.openhft.chronicle.jlbh.JLBHOptions
import net.openhft.chronicle.queue.ChronicleQueue
import org.example.Runner.withThread
import org.example.dto.NewOrderSingle
import org.example.dto.OrderCancelReplaceRequest
import org.example.message.ExchangeOrderListener
import kotlin.concurrent.thread


//Intel(R) Core(TM) i5-9300H CPU @ 2.40GHz, 2400 Mhz, 4 Core(s), 8 Logical Processor(s)
//-------------------------------- SUMMARY (end to end) us -------------------------------------------
//Percentile   run1         run2         run3         run4         run5      % Variation
//50.0:           12.11        13.20        12.91        12.91        12.69         2.62
//90.0:           18.53        20.32        19.30        18.91        18.40         6.50
//99.0:          225.02       136.45        98.18        75.39        66.94        40.90
//99.7:         2904.06      1894.40      1025.02       258.30       179.97        86.40
//99.9:         3969.02      4612.10      6234.11      1423.36       625.66        85.67
//99.97:        6004.74      6561.79      9715.71      3575.81      3518.46        54.01
//99.99:        6971.39      7331.84     10731.52      4268.03      4513.79        50.24
//worst:        7413.76     16941.06     18972.67      7970.82      8765.44        47.92
//----------------------------------------------------------------------------------------------------
object BasicRunnerBenchmark {
    private const val THROUGHPUT = 100_000
    private const val RUN_TIME = 10

    @JvmStatic
    fun main(args: Array<String>) {
        IOTools.deleteDirWithFiles("temp")

        val riskProcessor = withThread(
            Service.create(
                "risk service",
                listOf("temp/risk/in"),
                "temp/risk/out",
                RiskServiceOut::class.java,
                ::RiskService
            )
        )

        val orderProcessor = withThread(
            Service.create(
                "order service",
                listOf("temp/risk/out"),
                "temp/order/out",
                OrderServiceOut::class.java,
                ::OrderService
            )
        )

        ChronicleQueue.single("temp/risk/in").use { riskIn ->
            ChronicleQueue.single("temp/order/out").use { orderOut ->
                val orderTailer = orderOut.createTailer("test")
                val jlbh = JLBH(
                    JLBHOptions()
                        .warmUpIterations(25_000)
                        .pauseAfterWarmupMS(500)
                        .throughput(THROUGHPUT)
                        .iterations(THROUGHPUT * RUN_TIME)
                        .runs(5)
                        .recordOSJitter(true)
                        .accountForCoordinatedOmission(false)
                        .jlbhTask(OrderExecutionTask(riskIn))
                )
                val last = thread(start = false) {
                    val reader = orderTailer.methodReader(object : ExchangeOrderListener {
                        override fun newOrderSingle(nos: NewOrderSingle) {
                            jlbh.sampleNanos(System.nanoTime() - nos.eventTime)
                        }

                        override fun orderCancelReplaceRequest(ocrr: OrderCancelReplaceRequest) {
                            TODO("Not yet implemented")
                        }
                    })
                    while (!Thread.currentThread().isInterrupted) {
                        reader.readOne()
                    }
                }
                last.start()
                jlbh.start()
                riskProcessor.interrupt()
                orderProcessor.interrupt()
                last.interrupt()
            }
        }
    }
}