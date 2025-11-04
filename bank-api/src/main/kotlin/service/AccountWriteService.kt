package com.lecture.bank.service

import com.lecture.bank.common.ApiResponse
import com.lecture.bank.core.common.CircuitBreakerUtils.execute
import com.lecture.bank.core.common.TxAdvice
import com.lecture.bank.core.lock.DistributedLockService
import com.lecture.bank.domain.entity.Account
import com.lecture.bank.domain.entity.Transaction
import com.lecture.bank.domain.entity.TransactionType
import com.lecture.bank.domain.event.AccountCreatedEvent
import com.lecture.bank.domain.event.TransactionCreatedEvent
import com.lecture.bank.domain.repository.AccountRepository
import com.lecture.bank.domain.repository.TransactionRepository
import com.lecture.bank.event.publisher.EventPublisher
import com.lecture.bank.monitoring.metrics.BankMetrics
//import com.sun.org.slf4j.internal.LoggerFactory
// TODo
import org.slf4j.LoggerFactory
import dto.AccountView
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.math.BigDecimal


@Service
class AccountWriteService (
    private val txAdvice : TxAdvice,
    private val circuitBreaker: CircuitBreakerRegistry,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val lockService: DistributedLockService,
    private val eventPublisher : EventPublisher,
    private val bankMetrics : BankMetrics
) {
    private val logger = LoggerFactory.getLogger(AccountWriteService::class.java)
    private val breaker = circuitBreaker.circuitBreaker("accountWrite")

    private fun randomAccountNumber() : String {
        return System.currentTimeMillis().toString()
    }

    fun createAccount(name: String, balance : BigDecimal) : ResponseEntity<ApiResponse<AccountView>> {
        return breaker.execute(
            operation = {
                val account = txAdvice.run {
                    val accountNumber = randomAccountNumber()
                    val account = Account(
                        accountNumber = accountNumber,
                        balance = balance,
                        accountHolderName = name
                    )
                    accountRepository.save(account)
                }!!

                bankMetrics.incrementAccountCreated()
                bankMetrics.updateAccountCount(accountRepository.count())

                eventPublisher.publishAsync(
                    AccountCreatedEvent(
                        accountId =  account.id,
                        accountNumber = account.accountNumber,
                        accountHolderName = account.accountHolderName,
                        initialBalance = account.balance
                    )
                )

                return@execute ApiResponse.success(
                    data = AccountView(
                        id = account.id,
                        accountNumber = account.accountNumber,
                        balance = account.balance,
                        accountHolderName = account.accountHolderName,
                        createdAt = account.createdAt,
                    ),
                    msg = "Account Created"
                )
            },
            fallback = { exception ->
                logger.warn("Create Account Failed", exception)
                ApiResponse.error<AccountView>(
                    msg = "Create Account Failed",
                )
            }
        )
    }

    fun transfer(fromAccount : String, toAccount: String, amount: BigDecimal) : ResponseEntity<ApiResponse<String>> {
        return breaker.execute(
            operation = {
                lockService.executeWithTransactionLock(fromAccount, toAccount) {
                    transferInternal(fromAccount,toAccount, amount)
                }
            },
            fallback = { exception ->
                logger.warn("transfer Failed", exception)
                ApiResponse.error<String>(
                    msg = "transfer Failed",
                )
            }
        )!!
    }

    private fun transferInternal(
        fromAccount: String,
        toAccount: String,
        amount: BigDecimal
    ): ResponseEntity<ApiResponse<String>> {
        val transactionResult = txAdvice.run {
            val fromAcct = accountRepository.findByAccountNumber(fromAccount)

            if (fromAcct == null) {
                return@run null to "From Account not found"
            }

            if (fromAcct.balance < amount) {
                return@run  null to "From Account Balance limit"
            }

            val toAcct = accountRepository.findByAccountNumber(toAccount)

            if (toAcct == null) {
                return@run null to "To Account not found"
            }

            fromAcct.balance = fromAcct.balance.subtract(amount)
            toAcct.balance = toAcct.balance.add(amount)

            val savedFromAccount = accountRepository.save(fromAcct)
            val savedToAccount = accountRepository.save(toAcct)

            val fromTransaction = Transaction(
                account = fromAcct,
                amount = amount,
                type = TransactionType.TRANSFER,
                description = "Transfer From"
            )

            val fromSavedTransaction = transactionRepository.save(fromTransaction)

            val toTransaction = Transaction(
                account = toAcct,
                amount = amount,
                type = TransactionType.TRANSFER,
                description = "Transfer To"
            )

            val savedToTransaction = transactionRepository.save(toTransaction)

            bankMetrics.incrementTransaction("TRANSFER")
            bankMetrics.incrementTransaction("TRANSFER")

            return@run Pair(
                listOf(
                    Pair(fromSavedTransaction, savedFromAccount),
                    Pair(savedToTransaction, savedToAccount)
                ),
                null
            )
        }!!

        if (transactionResult.first == null) {
            return ApiResponse.error(transactionResult.second!!)
        }

        transactionResult.first!!.forEach { (savedTransaction, savedAccount) ->
            eventPublisher.publishAsync(
                TransactionCreatedEvent(
                    transactionId = savedTransaction.id,
                    accountId = savedAccount.id,
                    type = TransactionType.TRANSFER,
                    description = "Transaction Created",
                    amount = amount,
                    balanceAfter = savedAccount.balance,
                )
            )
        }

        return ApiResponse.success<String>(
            data = "Transfer Completed",
            msg = "Transfer Completed"
        )
    }

 }