package com.lecture.bank.exception

import com.lecture.bank.common.ApiResponse
import com.lecture.bank.core.exception.AccountNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // AccountNotFoundException 발생 시 handler를 통해 customize하기 위해 사용
    @ExceptionHandler(AccountNotFoundException::class)
    fun handleAccountNotFound(
        ex : AccountNotFoundException,
        req: WebRequest
    ) : ResponseEntity<ApiResponse<Nothing>> {  // return type, 찾지 못하였기에 nothing
        logger.warn("Account not found", ex)

        val response = ApiResponse.exceptionError<Nothing>(
            msg = ex.message ?: "Account not found",    // 상태코드 임의로 추가 가능
            errCode = "Account Not Found",
            path = getPath(req)
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    // path에 대한 경로 주는 함수
    // takeIf > kotlin의 표준 함수, 만족 시 객체 return, else null return
    private fun getPath(req : WebRequest) : String? {
        return req.getDescription(false).removePrefix("uri=").takeIf { it.isNotBlank() }
    }
}