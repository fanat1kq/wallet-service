package ru.example.walletservice.service.impl.walletIdempotency;

import org.springframework.stereotype.Component;
import ru.example.walletservice.dto.request.WalletRequestDto;
import ru.example.walletservice.dto.response.WalletResponseDto;
import ru.example.walletservice.entity.WalletOperationRecord;
import ru.example.walletservice.exception.IdempotencyConflictException;


@Component
public class WalletOperationIdempotencyChecker {

    private static boolean payloadMatches(WalletOperationRecord stored, WalletRequestDto request) {
        return stored.getWalletId().equals(request.walletId())
            && stored.getOperationType() == request.operationType()
            && stored.getAmount().compareTo(request.amount()) == 0;
    }

    public WalletResponseDto responseIfSamePayload(WalletOperationRecord storedOperation,
                                                   WalletRequestDto request) {
        if (!payloadMatches(storedOperation, request)) {
            throw new IdempotencyConflictException();
        }
        return new WalletResponseDto(
            storedOperation.getWalletId().toString(),
            storedOperation.getResultingBalance()
        );
    }
}
