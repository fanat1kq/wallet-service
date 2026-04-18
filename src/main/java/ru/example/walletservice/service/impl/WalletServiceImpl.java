package ru.example.walletservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.example.walletservice.dto.request.WalletRequestDto;
import ru.example.walletservice.dto.response.WalletResponseDto;
import ru.example.walletservice.facade.command.WalletCommandService;
import ru.example.walletservice.facade.query.WalletQueryService;
import ru.example.walletservice.service.WalletService;

import java.util.UUID;

/**
 * Реализация {@link WalletService} для веб-слоя: только склеивает query- и command-сценарии.
 * Детали — в {@link WalletQueryService} и {@link WalletCommandService}.
 */
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletQueryService walletQueryService;
    private final WalletCommandService walletCommandService;

    @Override
    public WalletResponseDto getWallet(UUID id) {
        return walletQueryService.getWallet(id);
    }

    @Override
    public WalletResponseDto processTransaction(UUID idempotencyKey,
                                                WalletRequestDto walletRequestDto) {
        return walletCommandService.processTransaction(idempotencyKey, walletRequestDto);
    }
}
