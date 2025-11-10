package com.lecture.bank.event.publisher

import com.lecture.bank.domain.event.DomainEvent
import com.lecture.bank.monitoring.metrics.BankMetrics
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

// DomainEvent을 외부에서 받아 사용
interface EventPublisher {
    fun publish(event : DomainEvent)
    fun publishAsync(event : DomainEvent)
    fun publishAll(events : List<DomainEvent>)
    fun publishAllAsync(events : List<DomainEvent>)
}
// evnet 전송 > listener 에서 event 처리
@Component
class EventPublisherImpl(
    // ApplicationEventPublisher : app 내에서 event 개시 (Redis, Kafka로 추후 분리 가능)
    private val eventPublisher : ApplicationEventPublisher,
    //
    private val metrics : BankMetrics,
) : EventPublisher {
    private val logger = LoggerFactory.getLogger(EventPublisherImpl::class.java)

    // <DLQ> (Dead Letter Queue) : 실패한 event에 대해 따로 Queue로 관리 > 실패한 이벤트 재처리 가능
    // Kafka의 topic, Redis의 key

    // cf. 여기는 하나의 instance에서 구현이라 Queue로 보관 없음

    override fun publish(event : DomainEvent) {
        logger.info("Publishing event: $event")

        try {
            eventPublisher.publishEvent(event)
            metrics.incrementEventPublished(event::class.simpleName!! ?: "Unknown")
        } catch (e : Exception) {
            logger.error("Error publishing event!", e)
        }
    }
    // 비동기 example (thread 처리)
    // taskExecutor >> config >>
    @Async("taskExecutor")
    override fun publishAsync(event : DomainEvent) {
        logger.info("Publishing event: $event")
        try {
            eventPublisher.publishEvent(event)
            metrics.incrementEventPublished(event::class.simpleName!! ?: "Unknown")
        } catch (e : Exception) {
            logger.error("Error publishing event!", e)
        }
    }
    override fun publishAll(events: List<DomainEvent>) {
        events.forEach { event ->
            eventPublisher.publishEvent(event)
            metrics.incrementEventPublished(event::class.simpleName!! ?: "Unknown")
        }
    }
    // 비동기 example (thread 처리)
    @Async("taskExecutor")
    override fun publishAllAsync(events : List<DomainEvent>) {
        events.forEach { event ->
            eventPublisher.publishEvent(event)
            metrics.incrementEventPublished(event::class.simpleName!! ?: "Unknown")
        }
    }

}