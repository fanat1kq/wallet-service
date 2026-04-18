package ru.example.walletservice.service.impl.operation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.example.walletservice.dto.request.WalletRequestDto;
import ru.example.walletservice.exception.OperationTypeNotFoundException;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WalletOperationDispatcher {

    private final List<WalletOperation> operations;

    public BigDecimal execute(WalletRequestDto walletRequestDto, BigDecimal balance) {
        return operations.stream()
            .filter(op -> op.supports(walletRequestDto.operationType()))
            .findFirst()
            .orElseThrow(OperationTypeNotFoundException::new)
            .apply(walletRequestDto.walletId(), balance, walletRequestDto.amount());
    }
}