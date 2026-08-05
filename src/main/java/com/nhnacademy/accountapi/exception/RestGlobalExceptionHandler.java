package com.nhnacademy.accountapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 클래스
 */
@Slf4j
@RestControllerAdvice
public class RestGlobalExceptionHandler {

    // 사용자 이미 존재하는 예외 처리
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.error("UserAlreadyExistsException: {}", ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // 사용자 잘못된 요청 예외 처리
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserBadRequestException.class)
    public ResponseEntity<String> handleUserBadRequestException(UserBadRequestException ex) {
        log.error("UserBadRequestException: {}", ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 사용자 권한 없음 예외 처리
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UserNotAllowException.class)
    public ResponseEntity<String> handleUserNotAllowException(UserNotAllowException ex) {
        log.error("UserNotAllowException: {}", ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // 사용자 찾을 수 없음 예외 처리
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("UserNotFoundException: {}", ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // request DTO의 valid 예외(검증 실패) 처리
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException: {}", ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityException(DataIntegrityViolationException ex) {
        return new ResponseEntity<>("데이터베이스 제약 조건 위반 (중복 등)", HttpStatus.CONFLICT);
    }

    // 나머지 서버 에러 처리
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ResponseEntity<String> handleInternalException(Exception ex) {
        log.error("InternalServerError: {}", ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
