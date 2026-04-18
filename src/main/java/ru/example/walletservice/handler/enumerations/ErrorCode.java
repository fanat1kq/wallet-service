package ru.example.walletservice.handler.enumerations;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.Map;

public enum ErrorCode {
    BAD_REQUEST,
    IDEMPOTENCY_CONFLICT,
    NOT_FOUND,
    BUSINESS_ERROR,
    VALIDATION_ERROR,
    INTERNAL_SERVER_ERROR;

    private static final Map<ErrorCode, ErrorInfo> INFO = Map.of(
        BAD_REQUEST, new ErrorInfo("BAD_REQUEST", "Bad Request", HttpStatus.BAD_REQUEST),
        IDEMPOTENCY_CONFLICT,
        new ErrorInfo("IDEMPOTENCY_CONFLICT", "Idempotency Conflict", HttpStatus.CONFLICT),
        NOT_FOUND, new ErrorInfo("NOT_FOUND", "Resource Not Found", HttpStatus.NOT_FOUND),
        BUSINESS_ERROR,
        new ErrorInfo("BUSINESS_ERROR", "Business Logic Error", HttpStatus.BAD_REQUEST),
        VALIDATION_ERROR,
        new ErrorInfo("VALIDATION_ERROR", "Validation Error", HttpStatus.UNPROCESSABLE_ENTITY),
        INTERNAL_SERVER_ERROR, new ErrorInfo("INTERNAL_SERVER_ERROR", "Internal Server Error",
            HttpStatus.INTERNAL_SERVER_ERROR)
    );

    public String getCode() {
        return INFO.get(this).code;
    }

    public String getDefaultTitle() {
        return INFO.get(this).title;
    }

    public HttpStatus getDefaultStatus() {
        return INFO.get(this).status;
    }

    public ProblemDetail createProblemDetail(String detail) {
        return createProblemDetail(detail, null);
    }

    public ProblemDetail createProblemDetail(String detail, Map<String, Object> properties) {
        var pd = ProblemDetail.forStatus(getDefaultStatus());
        pd.setTitle(getDefaultTitle());
        pd.setDetail(detail != null ? detail : getDefaultTitle());
        pd.setProperty("errorCode", getCode());
        if (properties != null) {
            properties.forEach(pd::setProperty);
        }
        return pd;
    }

    private record ErrorInfo(String code, String title, HttpStatus status) {
    }
}