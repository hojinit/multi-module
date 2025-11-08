package com.lecture.bank.common

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.ResponseEntity

// API Response fromat 규격화
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success : Boolean,
    val message : String,
    val data : T? = null,   // error인 경우 null
    val err : Error? = null
) {
    companion object {
        // ResponseEntity : spring 공식 제공 entity
        fun <T> success(data: T, msg : String = "Success"): ResponseEntity<ApiResponse<T>> {
            return ResponseEntity.ok(ApiResponse(true, msg, data))
        }

        fun <T> error(
            msg : String,
            errCode : String? = null,
            details : Any? = null,  // error 객체
            path : String? = null
        ) : ResponseEntity<ApiResponse<T>> {
            return ResponseEntity.badRequest().body(
                ApiResponse(false, msg, null, Error(errCode, details, path))
            )
        }
        // 추가적인 상황에 대한 처리
        fun <T> exceptionError(
            msg : String,
            errCode : String? = null,
            details : Any? = null,
            path : String? = null
        ) : ApiResponse<T> {
            return ApiResponse(false, msg, null, Error(errCode, details, path))
        }
    }
}

data class Error (
    val code : String? = null,
    val details : Any? = null,
    val path : String? = null
)