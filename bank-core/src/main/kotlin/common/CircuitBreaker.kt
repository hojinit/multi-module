package com.lecture.bank.core.common

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

// CircuitBreaker : Application의 회복성에 관련된 중요한 디자인 패턴 (전기회로 차단기 기반하여 구성됨)
// 특정 로직에서 에러가 지속 발생 시 자원 낭비를 막기 위해 일시적으로 차단
// >> 장애 발생 시 서비스가 반응하여 해당 서비스 일시적으로 차단하여 다른 서비스로 장애 전파되는 것을 막을 수 있다

// 1. close 상태 : 시스템이 정상이 경우
// CircuitBreaker에서 요청의 성공여부가 설정한 실패 횟수에 도달 시 close에서 open상태로 변경
// 2. open 상태 : 모든 요청 차단, 다른 서비스의 내부로직을 실행하지 않고 즉시 오류 리턴 (Fallback)
// 내부적 타이머가 만료될 때까지 요청 차단하며 만료 시 Half Open상태
// 3. Half Open : 서비스가 정상인지 테스트

@Configuration
class CircuitBreakerConfiguration {

    @Bean
    fun circuitBreakerRegistry() : CircuitBreakerRegistry {
        val config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50f) // 실패율 임계값 == 50%, 호출 실패율이 50% 넘어면 OPEN 상태
            .waitDurationInOpenState(Duration.ofSeconds(30)) // Open -> Half Open 상태의 대기 시간
            .permittedNumberOfCallsInHalfOpenState(3) //  Half Open 상태에서의 상태 기준 값
            .slidingWindowSize(5) //  최근 5개 항목만 검사
            .minimumNumberOfCalls(3) // 실패율 계산 위한 최소 호출 개수
            .build()
        /*
            1. 최근 5번의 호출 중에서 3번 이상이 필요하고, 이 기준에서 50%를 넘어선다면 -> Open
            2. Open에서 30초 대기한 다음에 half Open
            3. Half Open 상태에서 3번의 호출을 허용하면서 테스트 해보고 다음 상태를 결정 짓는다.
               성공 시 closed 상태, 실패 시 open 상태
         */
        return CircuitBreakerRegistry.of(config)
    }
}
// Singleton
object CircuitBreakerUtils {

    fun <T> CircuitBreaker.execute(
        operation : () -> T,
        fallback : (Exception) -> T
    ) : T { // return type
        return try {
            val supplier = CircuitBreaker.decorateSupplier(this) { operation() }
            supplier.get()
        } catch (e : Exception) {
            fallback(e)
        }
    }
}