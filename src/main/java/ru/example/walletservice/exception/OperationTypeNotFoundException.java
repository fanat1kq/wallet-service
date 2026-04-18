package ru.example.walletservice.exception;

import static ru.example.walletservice.util.ErrorMessages.MESSAGE_OPERATION_TYPE_NOT_FOUND;

public class OperationTypeNotFoundException extends RuntimeException {

    public OperationTypeNotFoundException() {
        super(MESSAGE_OPERATION_TYPE_NOT_FOUND);
    }
}