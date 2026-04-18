package ru.example.walletservice.exception;

import java.math.BigDecimal;
import java.util.UUID;

import static ru.example.walletservice.util.ErrorMessages.MESSAGE_INSUFFICIENT_FUNDS;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(UUID walletId, BigDecimal balance) {
        super(MESSAGE_INSUFFICIENT_FUNDS.formatted(walletId, balance));
    }
}