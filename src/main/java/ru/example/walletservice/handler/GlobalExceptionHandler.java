package ru.example.walletservice.handler;


import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.example.walletservice.exception.IdempotencyConflictException;
import ru.example.walletservice.exception.InsufficientFundsException;
import ru.example.walletservice.exception.MaxBalanceExceededException;
import ru.example.walletservice.exception.WalletNotFoundException;
import ru.example.walletservice.handler.enumerations.ErrorCode;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice(basePackages = "ru.example.walletservice")
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        return buildError(ex, ErrorCode.BAD_REQUEST);
    }

    @ExceptionHandler({WalletNotFoundException.class, NoSuchElementException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        return buildError(ex, ErrorCode.NOT_FOUND);
    }

    @ExceptionHandler({InsufficientFundsException.class, MaxBalanceExceededException.class})
    public ProblemDetail handleBusinessLogicErrors(RuntimeException ex) {
        return buildError(ex, ErrorCode.BUSINESS_ERROR);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ProblemDetail handleIdempotencyConflict(IdempotencyConflictException ex) {
        return buildError(ex, ErrorCode.IDEMPOTENCY_CONFLICT);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ProblemDetail handleValidationErrors(Exception ex) {
        List<String> errors;

        if (ex instanceof MethodArgumentNotValidException manv) {
            errors = manv.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .toList();
        } else if (ex instanceof ConstraintViolationException cve) {
            errors = cve.getConstraintViolations().stream()
                .map(violation -> String.format("%s: %s", violation.getPropertyPath(),
                    violation.getMessage()))
                .toList();
        } else {
            errors = List.of();
        }

        log.warn("Validation errors: {}", errors);

        return ErrorCode.VALIDATION_ERROR.createProblemDetail(
            errors.isEmpty() ? "Validation failed" : errors.get(0),
            Map.of("errors", errors)
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingRequestHeader(MissingRequestHeaderException ex) {
        String message = "Отсутствует обязательный заголовок: " + ex.getHeaderName();
        return ErrorCode.VALIDATION_ERROR.createProblemDetail(
            message,
            Map.of("errors", List.of(message))
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Некорректный формат параметра: " + ex.getName();
        return ErrorCode.VALIDATION_ERROR.createProblemDetail(
            message,
            Map.of("errors", List.of(message))
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllUncaught(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ErrorCode.INTERNAL_SERVER_ERROR.createProblemDetail("Internal server error");
    }

    private ProblemDetail buildError(RuntimeException ex, ErrorCode errorCode) {
        log.warn("{}: {}", errorCode.getDefaultTitle(), ex.getMessage());
        return errorCode.createProblemDetail(ex.getMessage());
    }
}