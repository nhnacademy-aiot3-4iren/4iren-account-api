package com.nhnacademy.accountapi.exception;

import com.nhnacademy.accountapi.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * 전역 예외 처리 클래스
 */
@Slf4j
@RestControllerAdvice
public class RestGlobalExceptionHandler {

    // 사용자 이미 존재하는 예외 처리
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.error("UserAlreadyExistsException: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.of(HttpStatus.CONFLICT.value(), ex.getMessage())
        );
    }

    // 사용자 잘못된 요청 예외 처리
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserBadRequestException.class)
    public ResponseEntity<ErrorResponse> handleUserBadRequestException(UserBadRequestException ex) {
        log.error("UserBadRequestException: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), ex.getMessage())
        );
    }

    // 사용자 권한 없음 예외 처리
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UserNotAllowException.class)
    public ResponseEntity<ErrorResponse> handleUserNotAllowException(UserNotAllowException ex) {
        log.error("UserNotAllowException: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.of(HttpStatus.FORBIDDEN.value(), ex.getMessage())
        );
    }

    // 사용자 찾을 수 없음 예외 처리
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("UserNotFoundException: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(HttpStatus.NOT_FOUND.value(), ex.getMessage())
        );
    }

    // request DTO의 valid 예외(검증 실패) 처리
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException: {}", ex.getMessage());

        String errorMessage=ex.getBindingResult().getFieldErrors().stream()
                .map(error->error.getDefaultMessage())
                .findFirst()
                .orElse("요청 데이터가 올바르지 않습니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), errorMessage)
        );
    }

    // 데이터 무결성 위반 예외 처리
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityException(DataIntegrityViolationException ex) {
        log.error("DataIntegrityViolationException: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.of(HttpStatus.CONFLICT.value(), "login-id 혹은 email이 이미 사용 중입니다.")
        );
    }

    // 나머지 서버 에러 처리
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleInternalException(Exception ex) {
        log.error("InternalServerError: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부에 오류가 발생했습니다. 관리자에게 문의하세요.")
        );
    }
}
