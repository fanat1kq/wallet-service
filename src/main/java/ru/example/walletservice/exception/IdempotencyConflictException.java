package ru.example.walletservice.exception;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException() {
        super("Idempotency-Key уже использован с другим payload");
    }
}
