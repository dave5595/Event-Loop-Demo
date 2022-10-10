package org.example

import me.oms.vcm.service.org.example.RiskServiceIn
import me.oms.vcm.service.org.example.RiskServiceOut
import net.openhft.chronicle.core.io.IOTools
import net.openhft.chronicle.core.threads.HandlerPriority
import net.openhft.chronicle.jlbh.JLBH
import net.openhft.chronicle.jlbh.JLBHOptions
import net.openhft.chronicle.jlbh.JLBHTask
import net.openhft.chronicle.queue.ChronicleQueue
import net.openhft.chronicle.wire.Base64LongConverter
import org.example.dto.*
import org.example.message.ExchangeOrderListener
import kotlin.concurrent.thread

//Intel(R) Core(TM) i5-9300H CPU @ 2.40GHz, 2400 Mhz, 4 Core(s), 8 Logical Processor(s)
//-------------------------------- SUMMARY (end to end) us -------------------------------------------
//Percentile   run1         run2         run3         run4         run5      % Variation
//50.0:           13.71        14.51        14.42        14.19        14.29         1.48
//90.0:           20.32        22.30        21.28        21.02        21.28         3.90
//99.0:          136.96      8101.89       133.38        94.59       213.25        98.26
//99.7:         2420.74     19496.96      1931.26       713.73      5316.61        94.61
//99.9:         5316.61     25853.95      3739.65      3338.24     11321.34        81.81
//99.97:        7233.54     28803.07      5562.37      4825.09     14565.38        76.81
//99.99:        8830.98     29589.50      6447.10      5414.91     15450.11        74.85
//worst:       80347.14     30048.26      9158.66     13090.82     15941.63        60.33
//----------------------------------------------------------------------------------------------------
object ServiceWrapperBenchmark {
    private const val THROUGHPUT = 100_000
    private const val RUN_TIME = 10

    @JvmStatic
    fun main(args: Array<String>) {
        IOTools.deleteDirWithFiles("temp")

        ServiceWrapper.builder(
            listOf("temp/risk/in"),
            "temp/risk/out",
            RiskServiceOut::class.java,
            ::RiskService
        ).priority(HandlerPriority.HIGH).build()

        ServiceWrapper.builder(
            listOf("temp/risk/out"),
            "temp/order/out",
            OrderServiceOut::class.java,
            ::OrderService
        ).priority(HandlerPriority.HIGH).build()

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
                val last = thread{
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
                jlbh.start()
                last.interrupt()
            }
        }
    }
}

class OrderExecutionTask(riskIn: ChronicleQueue) : JLBHTask {
    private lateinit var jlbh: JLBH
    private val input = riskIn.acquireAppender().methodWriter(RiskServiceIn::class.java)
    private val accountId = 123L
    private val symbol = Base64LongConverter.INSTANCE.parse("CIMB")
    private val currency = Base64LongConverter.INSTANCE.parse("MYR")
    private var request = newBuyLimitRequest(123, accountId)

    override fun init(jlbh: JLBH) {
        this.jlbh = jlbh
    }

    override fun run(startTimeNS: Long) {
        input.newOrderRequest(request.eventTime(startTimeNS))
    }

    private fun newBuyLimitRequest(id: Long, accountId: Long): NewOrderRequest {
        val request = NewOrderRequest()
        request.extOrdId = id
        request.quantity = 1.0
        request.price = 0.001
        request.symbol = symbol
        request.currency = currency
        request.side = OrderSide.Bid
        request.type = OrderType.Limit
        request.accountId = accountId
        return request
    }

}
