package com.lecture.bank.service

import com.lecture.bank.common.ApiResponse
import com.lecture.bank.core.common.CircuitBreakerUtils.execute
import com.lecture.bank.core.common.TxAdvice
import com.lecture.bank.domain.repository.AccountReadViewRepository
import com.lecture.bank.domain.repository.TransactionReadViewRepository
import dto.AccountView
import dto.TransactionView
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service


@Service
class AccountReadService(
    private val txAdvice : TxAdvice,
    private val accountViewRepository: AccountReadViewRepository,
    private val transactionViewRepository: TransactionReadViewRepository,
    private val circuitBreaker: CircuitBreakerRegistry
) {
    private val logger = LoggerFactory.getLogger(AccountReadService::class.java)
    private val breaker = circuitBreaker.circuitBreaker("accountRead")

    // account number로 Account 객체 return
    fun getAccount(accountNumber : String) : ResponseEntity<ApiResponse<AccountView>> {
        return breaker.execute(
            operation = {
                txAdvice.readOnly {
                    val response = accountViewRepository.findByAccountNumber(accountNumber)

                    return@readOnly if (response.isEmpty) {
                        ApiResponse.error("Account not found")
                    } else {
                        ApiResponse.success(AccountView.fromReadView(response.get()))   // Optional type인 경우는 get으로 가져와야 함
                    }
                }!! // non-null assertion operator : 이 값은 절대 null 이 아니다는 것을 컴파일러에 강제로 알려줌
                // readonly를 사용하고 있기 때문에 T?(nullable)일 때, T로 강제 캐스팅 가능
            },
            fallback = {exception ->
                logger.warn("Get Account Failed", exception)
                ApiResponse.error<AccountView>(
                    msg = "Get Account Failed",
                )
            }
        )
    }

    fun transactionHistory(accountNumber : String, limit: Int?) : ResponseEntity<ApiResponse<List<TransactionView>>> {
        return breaker.execute(
            operation = {
                txAdvice.readOnly {
                    val accountReadViewEntity = accountViewRepository.findByAccountNumber(accountNumber)
                    if (accountReadViewEntity.isEmpty) {
                        return@readOnly ApiResponse.error("Account Not Found")
                    }

                    val transactionEntity = if (limit != null) {
                        // limit 개수만 가져오게 설정
                        transactionViewRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber).take(limit)
                    } else {
                        transactionViewRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber)
                    }

                    return@readOnly ApiResponse.success(transactionEntity.map { TransactionView.fromReadView(it) })
                }!!
            },
            fallback = {exception ->
                logger.warn("Get Transaction History Failed", exception)
                ApiResponse.error<List<TransactionView>>(
                    msg = "Get Transaction History Failed",
                )
            }
        )
    }

    fun allAccount() : ResponseEntity<ApiResponse<List<AccountView>>> {
        return breaker.execute(
            operation = {
                txAdvice.readOnly {
                    val response = accountViewRepository.findAll().map { AccountView.fromReadView(it) }
                    return@readOnly ApiResponse.success(response)
                }!!
            },
            fallback = {exception ->
                logger.warn("Get All Account Failed", exception)
                ApiResponse.error<List<AccountView>>(
                    msg = "Get All Account Failed",
                )
            }
        )
    }

}

