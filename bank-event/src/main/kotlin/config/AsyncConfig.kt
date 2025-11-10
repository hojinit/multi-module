package com.lecture.bank.event.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

// kotlin은 기본적으로 final이기 때문에 AOP proxy 생성 불가 spring에서는 아래 설정을 proxy로 감싸야 함
// 따라서 class 앞에 open을 주거나 build.gradle에서 plugin apply 부여
@Configuration  // spring에서 처리하는 설정 class 명시
@EnableAsync    // 비동기
class AsyncConfig {

    // Executor return하는 DI taskExecutor Bean 등록
    @Bean(name = ["taskExecutor"])
    fun taskExecutor() : Executor { // executor 비동기 작업 처리하기 위한 일종의 thread pool 제공 및 관리
        val executor = ThreadPoolTaskExecutor()
        // 평소 유지하는 thread 개수, 살아있으면서 대기
        executor.corePoolSize = 2
        executor.maxPoolSize = 5
        // 작업 Queue로 만약 thread가 maxPoolSize만큼 동작 중일 때
        // 들어오는 작업을 Queue에 담아두고 가용 가능한 thread가 생성되면 Queue에서 빼서 작업처리
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("bank-event")
        executor.initialize()
        return executor
    }
}

