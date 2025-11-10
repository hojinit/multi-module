package com.lecture.bank.core.lock

import org.slf4j.LoggerFactory
import exception.LockAcquisitionException
import org.redisson.api.RedissonClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

// 동시성 제어 및 분산 환경에서의 분산락 서비스 처리
// 1. key가 있는 지 주기적으로 확인
// 2. 내부적으로 key를 루아스크립트 형태로 원자적으로 획득 시도 하고 획득 시 채널을 구독하여 이벤트 처리 (Redis)

// 분산 락 사용
@ConfigurationProperties(prefix = "bank.lock")
data class LockProperties(
    val timeout : Long = 5000,
    val leaseTime : Long = 10000,   //release time out
    val retryInterval : Long = 100,
    val maxRetryAttempts: Long = 50
)

@Service
@EnableConfigurationProperties(LockProperties::class)
class DistributedLockService(
    private val redissonClient : RedissonClient,
    private val lockProperties: LockProperties,
) {
    private val logger = LoggerFactory.getLogger(DistributedLockService::class.java)

    // 내부 사용
    private fun <T> executeWithLock(
        lockKey : String,
        action : () -> T    // 익명함수 형태로 전달
    ) : T {
        val lock = redissonClient.getLock(lockKey)
        return try {
//            LockProperties 기준 획득, 대기하기 위해 사용 (FIFO)
            val acquired = lock.tryLock(
                lockProperties.timeout,
                lockProperties.leaseTime,
                TimeUnit.MILLISECONDS
            )

            if (!acquired) {
                logger.error("Acquiring lock for $lockKey")
                throw LockAcquisitionException("Acquiring lock for $lockKey")
            }

            try {
                action()
            } finally {
                if (lock.isHeldByCurrentThread) {
                    lock.unlock()
                }
            }
        } catch (e: Exception) {
            logger.error("Lock [$lockKey] failed to acquire lock", e)
            throw e
        }
    }

    // 외부 사용
    fun <T> executeWithAccountLock(
        accountNumber : String,
        action : () -> T
    ) : T {
        val lockKey = "account:lock:$accountNumber"
        return executeWithLock(lockKey, action)
    }

    fun <T> executeWithTransactionLock(
        from : String,
        to : String,
        action : () -> T
    ) : T {
        val sorted = listOf(from, to).sorted()  //deadlock 방지
        val lockKey = "transaction:lock:${sorted[0]}:${sorted[1]}"
        return executeWithLock(lockKey, action)
    }
}