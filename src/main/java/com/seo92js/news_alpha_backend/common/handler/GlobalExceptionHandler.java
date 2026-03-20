package com.seo92js.news_alpha_backend.common.handler;

import com.seo92js.news_alpha_backend.dto.ErrorResponse;
import com.seo92js.news_alpha_backend.domain.member.exception.DuplicateEmailException;
import com.seo92js.news_alpha_backend.common.exception.ErrorCode;
import com.seo92js.news_alpha_backend.domain.member.exception.MemberNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MemberNotFoundException.class)
    public ErrorResponse handleMemberNotFound(MemberNotFoundException e) {

        log.warn("MemberNotFoundException : {}", e.getLogMessage());
        return ErrorResponse.of(e.getErrorCode());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateEmailException.class)
    public ErrorResponse handleDuplicateEmail(DuplicateEmailException e) {

        log.warn("DuplicateEmailException : {}", e.getLogMessage());
        return ErrorResponse.of(e.getErrorCode());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e) {

        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.INVALID_INPUT.getMessage());

        log.warn("MethodArgumentNotValidException : {}", message);
        return ErrorResponse.of(ErrorCode.INVALID_INPUT, message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException e) {

        log.warn("HttpMessageNotReadableException : {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.INVALID_INPUT, "요청 본문을 읽을 수 없습니다.");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception e) {

        log.error("Unhandled exception : {}", e.getMessage(), e);
        return ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
