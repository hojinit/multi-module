package com.lecture.bank.domain.repository

import com.lecture.bank.domain.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<Account, Long> {
    // optional 을 사용할 수는 없음
    // jpa로는 method 이름을 통해 DB 쿼리를 자동화하기 떄문에 필드명을 잘 명시해야 함
    // jpa > 객체-관계 매핑 자동화 > 생산성 향상
    fun findByAccountNumber(accountNumber: String): Account?
}