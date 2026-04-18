package ru.example.walletservice.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.example.walletservice.handler.enumerations.ErrorCode;

@Slf4j
@RestControllerAdvice
public class StaticResourceNotFoundHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        log.debug("Static resource not found: {}", ex.getResourcePath());
        return ErrorCode.NOT_FOUND.createProblemDetail(ex.getMessage());
    }
}
