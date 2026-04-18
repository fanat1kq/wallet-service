package ru.example.walletservice.service.impl.operation.walletOperations;

import org.springframework.stereotype.Component;
import ru.example.walletservice.dto.enumerations.OperationType;
import ru.example.walletservice.exception.InsufficientFundsException;
import ru.example.walletservice.service.impl.operation.WalletOperation;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class WithdrawOperation implements WalletOperation {

    @Override
    public boolean supports(OperationType operationType) {
        return OperationType.WITHDRAW == operationType;
    }

    @Override
    public BigDecimal apply(UUID id, BigDecimal balance, BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(id, balance);
        }

        return balance.subtract(amount);
    }
}