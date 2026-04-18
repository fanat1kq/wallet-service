package ru.example.walletservice.exception;

import java.math.BigDecimal;

import static ru.example.walletservice.util.ErrorMessages.MESSAGE_MAX_BALANCE_EXCEEDED;

public class MaxBalanceExceededException extends RuntimeException {

    public MaxBalanceExceededException(BigDecimal amount) {
        super(MESSAGE_MAX_BALANCE_EXCEEDED.formatted(amount));
    }
}
