package ru.example.walletservice.exception;

import static ru.example.walletservice.util.ErrorMessages.MESSAGE_IDEMPOTENCY_CONFLICT;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException() {
        super(MESSAGE_IDEMPOTENCY_CONFLICT);
    }
}
