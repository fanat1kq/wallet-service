package ru.example.walletservice.exception;

import static ru.example.walletservice.util.ErrorMessages.MESSAGE_WALLET_NOT_FOUND;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String id) {
        super(MESSAGE_WALLET_NOT_FOUND.formatted(id));
    }
}