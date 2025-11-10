package com.lecture.bank.core.common

import com.lecture.bank.domain.entity.Account
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

// Transaction Advice 클래스로 Spring의 AOP 구현 및 db transaction 일관성 보장
// 1. Spring만 사용 시 DB 통신 ACID 보장을 위해 Transactional annotation을 많이 사용하나 문제가 존재
// 1.1 Self-Invocation : 같은 class 내부에서 method 호출 시 Proxy를 거치지 않기 때문에 의도한 transaction annotation이 동작 안함
// 2. transaction이 시작되는 경계가 모호
// 3. 예외처리 복잡성
// AOP, Proxy 제약성 및 컴파일 시 검증이 어려움

// 따라서 TxAdvice 패턴을 사용
// lamda 형태로 함수형 프로그램을 통한 transaction 관리하고 처리

interface TransactionRunner {
    fun <T> run (func: () -> T?) : T? // @Transactional
    fun <T> readOnly (func: () -> T?) : T? // @Transaction(readOnly = true)
    fun <T> runNew (func: () -> T?) : T? // @Transactional(Propagation.REQUIRES_NEW)
}

@Component
class TransactionAdvice : TransactionRunner {
    // 단일 책임 원칙에 따른 transaction 만 관리하고 담당하여 단순히 들어오는 function만 받아서 처리
    // generic을 사용하기에 다양한 타입을 다룰 수 있다
    @Transactional
    override fun <T> run (func: () -> T?) : T? = func()

    @Transactional(readOnly = true)
    override fun <T> readOnly (func: () -> T?) : T? = func()

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun <T> runNew (func: () -> T?) : T? = func()
}

@Component
class TxAdvice(
    private val advice : TransactionAdvice
) {
    // TxAdvice를 DI로 받고 원하는 transaction사용
    // 내부적으로 private한 값 관리 위해 추가 구성
    fun <T> run (func: () -> T?) : T? = advice.run(func)
    fun <T> readOnly (func: () -> T?) : T? = advice.readOnly(func)
    fun <T> runNew (func: () -> T?) : T? = advice.runNew(func)
}

//Transaction Advice 클래스 예시
/*
@Service
Class AccountService {

    @Transactional
    fun createAccountWithBonus(name: String):Account {

        val account = Account(name, BigDecimal.ZERO)
        accountRepository.save(account)

        // 문제 : 같은 class의 method 호출 시 proxy를 거치지 않음
        addWelcomeBonus(accout.id)  //@Transactional 무시
    }

    Transactional(propagation = Propagation.REQUIRES_NEW)   // Propagation 신규 생성
    fun addWelcomeBonus(accountId: Long){
        var account = accountRepository.findById(accountId.get())
        account.balane = account.balance.add(BigDecimal("1000"))
        accountRepository.save(account)

        // 예외 발생 시
        throw RuntimeException("보너스 지급 실패")
    }
}
*/