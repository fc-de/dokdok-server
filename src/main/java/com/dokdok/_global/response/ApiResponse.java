package com.dokdok._global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String code,
        String message,
        T data
) {

    // ========== CREATE (201 Created) ==========

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>("CREATED", "리소스가 생성되었습니다.", data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>("CREATED", message, data));
    }

    // ========== READ (200 OK) ==========

    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>("SUCCESS", "조회에 성공했습니다.", data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>("SUCCESS", message, data));
    }

    public static ResponseEntity<ApiResponse<Void>> success() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>("SUCCESS", "요청이 성공했습니다.", null));
    }

    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>("SUCCESS", message, null));
    }

    // ========== UPDATE (200 OK) ==========

    public static <T> ResponseEntity<ApiResponse<T>> updated(T data) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>("UPDATED", "리소스가 수정되었습니다.", data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> updated(T data, String message) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>("UPDATED", message, data));
    }

    public static ResponseEntity<ApiResponse<Void>> updated() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>("UPDATED", "리소스가 수정되었습니다.", null));
    }

    public static ResponseEntity<ApiResponse<Void>> updated(String message) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>("UPDATED", message, null));
    }

    // ========== DELETE (200 OK) ==========

    public static ResponseEntity<ApiResponse<Void>> deleted() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>("DELETED", "리소스가 삭제되었습니다.", null));
    }

    public static ResponseEntity<ApiResponse<Void>> deleted(String message) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>("DELETED", message, null));
    }

    // ========== ERROR ==========

    public static <T> ResponseEntity<ApiResponse<T>> error(String code, String message) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(code, message, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String code, String message, T data) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(code, message, data));
    }
}