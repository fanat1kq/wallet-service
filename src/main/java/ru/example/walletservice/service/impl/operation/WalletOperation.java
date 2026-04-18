package ru.example.walletservice.service.impl.operation;

import ru.example.walletservice.dto.enumerations.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletOperation {

    BigDecimal MAX_BALANCE = new BigDecimal("9999999999999.99");

    boolean supports(OperationType operationType);

    BigDecimal apply(UUID id, BigDecimal balance, BigDecimal amount);
}