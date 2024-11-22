package com.sysmatic2.finalbe.exception;

import com.sysmatic2.finalbe.util.ResponseUtils;
import jakarta.validation.ConstraintViolationException;
import org.apache.tomcat.websocket.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400: 커스텀 예외 처리 - ReplyNotFoundException
    @ExceptionHandler(ReplyNotFoundException.class)
    public ResponseEntity<Object> handleReplyNotFoundException(ReplyNotFoundException ex) {
        logger.warn("ReplyNotFoundException 발생: {}", ex.getMessage());
        return ResponseUtils.buildErrorResponse(
                "BAD_REQUEST",
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    // 400: 커스텀 예외 처리 - ConsultationAlreadyCompletedException
    @ExceptionHandler(ConsultationAlreadyCompletedException.class)
    public ResponseEntity<Object> handleConsultationAlreadyCompletedException(ConsultationAlreadyCompletedException ex) {
        logger.warn("ConsultationAlreadyCompletedException 발생: {}", ex.getMessage());
        return ResponseUtils.buildErrorResponse(
                "BAD_REQUEST",
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    // 500: 일반적인 예외 처리
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        logger.error("Unhandled exception occurred: ", ex);
        return ResponseUtils.buildErrorResponse(
                "INTERNAL_SERVER_ERROR",
                ex.getClass().getSimpleName(),
                "알 수 없는 오류가 발생했습니다.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // 500: 이메일 전송 실패
    @ExceptionHandler(MailException.class)
    public ResponseEntity<Object> handleMailException(MailException ex) {
        logger.error("Mail send failed: ", ex);
        // 발생 예외에 따라 세분화 필요?
        return ResponseUtils.buildErrorResponse(
                "MAIL_SEND_FAILED",
                ex.getClass().getSimpleName(),
                "메일 전송에 실패했습니다.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // 400: 잘못된 데이터 타입
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.warn("Invalid data format: {}", e.getMessage());
        return ResponseUtils.buildErrorResponse(
                "HTTP_MESSAGE_NOT_READABLE",
                e.getClass().getSimpleName(),
                "잘못된 데이터 타입입니다.",
                HttpStatus.BAD_REQUEST
        );
    }

    // 400: 유효성 검사 실패
    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class, ConfirmPasswordMismatchException.class, InvestmentAssetClassesNotActiveException.class})
    public ResponseEntity<Object> handleValidationExceptions(Exception ex) {
        logger.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException methodEx = (MethodArgumentNotValidException) ex;
            for (FieldError error : methodEx.getBindingResult().getFieldErrors()) {
                fieldErrors.put(error.getField(), error.getDefaultMessage());
            }
        } else if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException constraintEx = (ConstraintViolationException) ex;
            constraintEx.getConstraintViolations().forEach(violation -> {
                String field = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                fieldErrors.put(field, message);
            });
        } else if (ex instanceof ConfirmPasswordMismatchException) {
            ConfirmPasswordMismatchException confirmEx = (ConfirmPasswordMismatchException) ex;
            String field = "confirmPassword";
            String message = confirmEx.getMessage();
            fieldErrors.put(field, message);
        } else if (ex instanceof InvestmentAssetClassesNotActiveException) {
            InvestmentAssetClassesNotActiveException constraintEx = (InvestmentAssetClassesNotActiveException) ex;
            String field = "investmentAssetClasses";
            String message = constraintEx.getMessage();
            fieldErrors.put(field, message);
        }

        return ResponseUtils.buildFieldErrorResponse(
                fieldErrors,
                ex.getClass().getSimpleName(),
                "유효성 검사에 실패했습니다.",
                HttpStatus.BAD_REQUEST
        );
    }

    // 400: 이메일 인증 실패
    @ExceptionHandler(EmailVerificationFailedException.class)
    public ResponseEntity<Object> handleEmailVerificationFailedException(EmailVerificationFailedException e) {
        logger.warn("Email Verification failed: {}", e.getMessage());
        return ResponseUtils.buildErrorResponse(
                "EMAIL_VERIFICATION_FAILED",
                e.getClass().getSimpleName(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    // 400: 잘못된 파라미터 (타입 및 누락)
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleBadRequestExceptions(Exception ex) {
        logger.warn("Bad request parameter: {}", ex.getMessage());

        String message;
        if (ex instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException missingEx = (MissingServletRequestParameterException) ex;
            message = String.format("필수 요청 파라미터 '%s'가 누락되었습니다. 기대하는 타입: %s",
                    missingEx.getParameterName(), missingEx.getParameterType());
        } else if (ex instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException mismatchEx = (MethodArgumentTypeMismatchException) ex;
            message = String.format("파라미터 '%s'의 값 '%s'이(가) 잘못되었습니다. 기대되는 타입: %s",
                    mismatchEx.getName(),
                    mismatchEx.getValue(),
                    mismatchEx.getRequiredType() != null ? mismatchEx.getRequiredType().getSimpleName() : "알 수 없음");
        } else {
            message = "잘못된 요청입니다.";
        }

        return ResponseUtils.buildErrorResponse(
                "BAD_REQUEST",
                ex.getClass().getSimpleName(),
                message,
                HttpStatus.BAD_REQUEST
        );
    }

    // 401: 인증 실패
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException e) {
        logger.warn("Authentication failed: {}", e.getMessage());
        return ResponseUtils.buildErrorResponse(
                "UNAUTHORIZED",
                e.getClass().getSimpleName(),
                "로그인 정보가 없습니다.",
                HttpStatus.UNAUTHORIZED
        );
    }

    // 403: 권한 없음
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e) {
        logger.warn("Access denied: {}", e.getMessage());
        return ResponseUtils.buildErrorResponse(
                "FORBIDDEN",
                e.getClass().getSimpleName(),
                "권한이 없습니다.",
                HttpStatus.FORBIDDEN
        );
    }

    // 404: 데이터 없음
    @ExceptionHandler({NoSuchElementException.class, TradingTypeNotFoundException.class, TradingCycleNotFoundException.class, EmptyResultDataAccessException.class, InvestmentAssetClassesNotFoundException.class, ConsultationNotFoundException.class, TraderNotFoundException.class, InvestorNotFoundException.class, StrategyNotFoundException.class})
    public ResponseEntity<Object> handleNotFoundExceptions(Exception ex) {
        logger.warn("Data not found: {}", ex.getMessage());
        return ResponseUtils.buildErrorResponse(
                "NOT_FOUND",
                ex.getClass().getSimpleName(),
                "해당되는 데이터를 찾을 수 없습니다.",
                HttpStatus.NOT_FOUND
        );
    }

    // 405: 잘못된 요청 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        logger.warn("Invalid HTTP method: {}", ex.getMessage());
        return ResponseUtils.buildErrorResponse(
                "METHOD_NOT_ALLOWED",
                ex.getClass().getSimpleName(),
                "호출 메서드가 잘못되었습니다.",
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    // 409: 데이터 충돌
    @ExceptionHandler({DataIntegrityViolationException.class, DuplicateTradingTypeOrderException.class, DuplicateTradingCycleOrderException.class, MemberAlreadyExistsException.class})
    public ResponseEntity<Object> handleConflictExceptions(Exception ex) {
        logger.error("Data conflict: {}", ex.getMessage());

        String message;
        if (ex instanceof DuplicateTradingTypeOrderException || ex instanceof DuplicateTradingCycleOrderException || ex instanceof MemberAlreadyExistsException) {
            message = ex.getMessage();
        } else {
            message = "데이터베이스 제약 조건을 위반했습니다.";
        }

        return ResponseUtils.buildErrorResponse(
                "CONFLICT",
                ex.getClass().getSimpleName(),
                message,
                HttpStatus.CONFLICT
        );
    }





}