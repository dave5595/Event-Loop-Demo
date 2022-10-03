package me.oms.vcm.service.impl

import me.oms.common.Logger
import me.oms.common.snapshotter.IndexedSnapshot
import me.oms.core.Chronicle.Companion.toLongValue
import me.oms.core.service.Service
import me.oms.order.enum.OrderSide
import me.oms.vcm.dto.*
import me.oms.vcm.service.RiskServiceOut
import net.openhft.chronicle.core.values.LongValue
import net.openhft.chronicle.map.ChronicleMapBuilder
import java.io.File

class RiskService(private val out: RiskServiceOut) : AbstractRiskService(out) {
    private val log by Logger()
    private val accounts: MutableMap<Long, Account> = HashMap()
    private val orders: Map<LongValue, Order>
    private val accountCreated = AccountCreated()
    private val assetDeposited = AssetDeposited()

    init {
        orders = ChronicleMapBuilder
            .simpleMapOf(LongValue::class.java, Order::class.java)
            .entries(1_000_000)
            .createPersistedTo(File("temp/orders"))
    }

    override fun createAccount(create: CreateAccount) {
        val account = Account.new(create.accountId)
        accounts.computeIfAbsent(create.accountId) { account }
        out.accountCreated(emitAccountCreated(account))
        log.info("account created: $account")
    }

    override fun depositAsset(deposit: DepositAsset) {
        accounts[deposit.accountId]?.let { account ->
            val asset = getOrCreateAsset(deposit.symbol, account)
            asset.deposit(deposit.amount)
            emitAssetDeposited(account, deposit.symbol, deposit.amount)
            log.info("asset deposited: $asset")
        }
    }

    override fun replaceOrderRequest(request: ReplaceOrderRequest) {
        orders[request.orderId.toLongValue()]?.let { order ->
            accounts[order.accountId]?.let { account ->
                when (order.side) {
                    OrderSide.Bid -> bidTransactionManager.handleReplaceOrderRequest(request, account, order)
                    OrderSide.Ask -> askTransactionManager.handleReplaceOrderRequest(request, account, order)
                    else -> throw UnsupportedOperationException()
                }
            }
        }
    }

    override fun newOrderRequest(request: NewOrderRequest) {
        accounts[request.accountId]?.let { account ->
            when (request.side) {
                OrderSide.Bid -> bidTransactionManager.handleNewOrderRequest(request, account)
                OrderSide.Ask -> askTransactionManager.handleNewOrderRequest(request, account)

                else -> throw UnsupportedOperationException()
            }
        }
    }

    //AssetDeposited(MYR, 1000), freeBalance = 1000, balance = 1000, reserved = 0
    //AssetReserved(MYR, 50), freeBalance = 950, balance = 1000, reserved = 50
    //ReservedAssetDebited(MYR, 50), freeBalance = 950, balance = 950, reserved = 0
    //AssetDeposited(CIMB, 100), freeBalance = 100, balance = 100, reserved = 0, averagePrice = 1.3
    override fun orderFilled(filled: OrderFilled) {
        val accountId = filled.order.accountId
        accounts[accountId]?.let { account ->
            when (filled.order.side) {
                OrderSide.Bid -> bidTransactionManager.handleOrderFilled(filled, account)
                OrderSide.Ask -> askTransactionManager.handleOrderFilled(filled, account)
                else -> throw UnsupportedOperationException()
            }
        }
    }

    //AssetDeposited(MYR, 1000), freeBalance = 1000, balance = 1000
    //AssetReserved(MYR, 50), freeBalance = 950, balance = 1000
    //AssetUnreserved(MYR, 50), freeBalance = 1000, balance = 1000
    override fun orderRejected(rejected: OrderRejected) {
        accounts[rejected.order.accountId]?.let { account ->
            when (rejected.order.side) {
                OrderSide.Bid -> bidTransactionManager.handleOrderRejected(rejected, account)
                OrderSide.Ask -> askTransactionManager.handleOrderRejected(rejected, account)
                else -> throw UnsupportedOperationException()
            }
        }
    }

    override fun orderReplaced(replaced: OrderReplaced) {
        accounts[replaced.order.accountId]?.let { account ->
            orders[replaced.order.id.toLongValue()]?.let { order ->
                when (replaced.order.side) {
                    OrderSide.Bid -> bidTransactionManager.handleOrderReplaced(replaced, account, order)
                    OrderSide.Ask -> askTransactionManager.handleOrderReplaced(replaced, account, order)
                    else -> throw UnsupportedOperationException()
                }
            }
        }

    }

    private fun getOrCreateAsset(symbol: Long, acc: Account): Account.Asset {
        return acc.getAsset(symbol) ?: run {
            Account.Asset.create(symbol, 0.0).apply {
                acc.addAsset(this)
            }
        }
    }

    private fun emitAccountCreated(account: Account): AccountCreated {
        accountCreated.account = account
        return accountCreated
    }

    private fun emitAssetDeposited(account: Account, symbol: Long, amount: Double) {
        assetDeposited.reset()
        assetDeposited.accountId = account.id
        assetDeposited.symbol = symbol
        assetDeposited.deposited = amount
        assetDeposited.asset = account.getAsset(symbol)!!
        out.assetDeposited(assetDeposited)
    }

    override fun getState(): Any {
        return accounts
    }

    @Suppress("UNCHECKED_CAST")
    override fun snapshot(snapshot: IndexedSnapshot) {
        (snapshot.state as MutableMap<Long, Account>)
            .asSequence()
            .forEach { (accountId, account) ->
                accounts[accountId] = account
            }
    }
}



