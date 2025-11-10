package com.lecture.bank.monitoring.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong

// 모니터링 대표적으로는 Datadog, kibana, Granfana
// 디버깅 시 트래픽 유형을 metric 형태로 구성 하여 트래픽 유형 분석 가능

@Component
class BankMetrics(private val meterRegistry: MeterRegistry) { //MeterRegistry : Micrometer의 주요한 interface, metric 등록 및 관리
    // 단일 instance여서 계좌 수의 대한 게이지(실시간 변동하는 단일 값을 추적하는 metric) 누적, 단일이여서 Atomic으로 사용
    private val accountGauge = AtomicLong(0)

    init {
        // class 초기화 시 게이지를 메트릭에 등록하고 관리, accountGauge 모니터링
        meterRegistry.gauge("bank.account.total", accountGauge) {it.get().toDouble()}
    }

    // account Count가 생성 될 시 사용할 메트릭 정보 생성
    fun incrementAccountCreated() {
        Counter.builder("bank.account.created").description("Number of accounts created").register(meterRegistry).increment()
    }

    fun updateAccountCount(count: Long) {
        accountGauge.set(count)
    }

    fun incrementTransaction(type: String) {
        Counter.builder("bank.transaction.count")
            .description("Number of transactions")
            .tag("type", type)  // cf. tag가 붙게 되면 메타 데이터가 커지게 되니 유의해야 함, 태그로 분석을 사용하지는 않음
            .register(meterRegistry)
            .increment()
    }

    fun recordTransactionAmount(amount: BigDecimal, type: String) {
        // DistributionSummary : 분포 요약이라는 메트릭에 기록하게 됨
        // 값의 분포 (최소값, 최대값, 평균 등)이기에 특정 금액이나 수량 분석 시 유용
        DistributionSummary.builder("bank.transaction.amount")
            .description("Transaction amounts distribution")
            .tag("type", type)
            .register(meterRegistry)
            .record(amount.toDouble())
    }

    fun incrementEventPublished(eventType: String) {
        Counter.builder("bank.event.published")
            .description("Number of events published")
            .tag("type", eventType)
            .register(meterRegistry)
            .increment()
    }

    fun incrementEventProcessed(eventType: String) {
        Counter.builder("bank.event.processed")
            .description("Number of events processed")
            .tag("type", eventType)
            .tag("status", "success")
            .register(meterRegistry)
            .increment()
    }

    fun incrementEventFailed(eventType: String) {
        Counter.builder("bank.event.failed")
            .description("Number of events failed")
            .tag("type", eventType)
            .tag("status", "failed")
            .register(meterRegistry)
            .increment()
    }

    fun recordEventProcessingTime(duration: Duration, eventType: String) {
        // 시간 계산 시 사용
        Timer.builder("bank.event.processing.time")
            .description("Event processing time")
            .tag("type", eventType)
            .register(meterRegistry)
            .record(duration)
    }

    fun incrementLockAcquisitionFailure(lockKey: String) {
        Counter.builder("bank.lock.acquisition.failed")
            .description("Number of failed lock acquisitions")
            .tag("lock_key", lockKey)
            .register(meterRegistry)
            .increment()
    }

    fun incrementLockAcquisitionSuccess(lockKey: String) {
        Counter.builder("bank.lock.acquisition.success")
            .description("Number of successful lock acquisitions")
            .tag("lock_key", lockKey)
            .register(meterRegistry)
            .increment()
    }

    fun recordApiResponseTime(duration: Duration, endpoint: String, method: String) {
        Timer.builder("bank.api.response.time")
            .description("API response time")
            .tag("endpoint", endpoint)
            .tag("method", method)
            .register(meterRegistry)
            .record(duration)
    }
}