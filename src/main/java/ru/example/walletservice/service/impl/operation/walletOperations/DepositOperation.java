package ru.example.walletservice.service.impl.operation.walletOperations;

import org.springframework.stereotype.Component;
import ru.example.walletservice.dto.enumerations.OperationType;
import ru.example.walletservice.exception.MaxBalanceExceededException;
import ru.example.walletservice.service.impl.operation.WalletOperation;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Component
public class DepositOperation implements WalletOperation {

    @Override
    public boolean supports(OperationType operationType) {
        return OperationType.DEPOSIT == operationType;
    }

    @Override
    public BigDecimal apply(UUID id, BigDecimal balance, BigDecimal amount) {
        Objects.requireNonNull(id, "Wallet ID must not be null");
        Objects.requireNonNull(balance, "Balance must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");

        BigDecimal newBalance = balance.add(amount);
        if (newBalance.compareTo(WalletOperation.MAX_BALANCE) > 0) {
            throw new MaxBalanceExceededException(WalletOperation.MAX_BALANCE);
        }
        return newBalance;
    }
}