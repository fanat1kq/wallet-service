package ru.example.walletservice.facade.command.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.walletservice.dto.request.WalletRequestDto;
import ru.example.walletservice.dto.response.WalletResponseDto;
import ru.example.walletservice.entity.Wallet;
import ru.example.walletservice.entity.WalletOperationRecord;
import ru.example.walletservice.exception.WalletNotFoundException;
import ru.example.walletservice.facade.command.WalletCommandService;
import ru.example.walletservice.mapper.WalletMapper;
import ru.example.walletservice.mapper.WalletOperationMapper;
import ru.example.walletservice.repository.WalletOperationRecordRepository;
import ru.example.walletservice.repository.WalletRepository;
import ru.example.walletservice.service.impl.operation.WalletOperationDispatcher;
import ru.example.walletservice.service.impl.walletIdempotency.WalletOperationIdempotencyChecker;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletCommandServiceImpl implements WalletCommandService {

    private final WalletRepository walletRepository;
    private final WalletOperationRecordRepository walletOperationRecordRepository;
    private final WalletOperationDispatcher walletOperationDispatcher;
    private final WalletOperationMapper walletOperationMapper;
    private final WalletMapper walletMapper;
    private final WalletOperationIdempotencyChecker idempotencyChecker;

    @Override
    @Transactional
    public WalletResponseDto processTransaction(UUID operationId,
                                                WalletRequestDto walletRequestDto) {
        Optional<WalletOperationRecord> alreadyDone =
            walletOperationRecordRepository.findById(operationId);
        if (alreadyDone.isPresent()) {
            return idempotencyChecker.responseIfSamePayload(alreadyDone.get(), walletRequestDto);
        }

        Wallet wallet = walletRepository.findByIdForUpdate(walletRequestDto.walletId())
            .orElseThrow(() -> new WalletNotFoundException(walletRequestDto.walletId().toString()));

        BigDecimal newBalance =
            walletOperationDispatcher.execute(walletRequestDto, wallet.getBalance());

        WalletOperationRecord operationRecord =
            walletOperationMapper.toEntity(operationId, walletRequestDto, newBalance);

        try {
            walletOperationRecordRepository.saveAndFlush(operationRecord);
        } catch (DataIntegrityViolationException ex) {
            WalletOperationRecord concurrent =
                walletOperationRecordRepository.findById(operationId).orElseThrow(() -> ex);
            return idempotencyChecker.responseIfSamePayload(concurrent, walletRequestDto);
        }

        wallet.setBalance(newBalance);
        return walletMapper.toResponseDto(wallet);
    }
}
