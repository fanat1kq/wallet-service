package ru.example.walletservice.facade.query.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.walletservice.dto.response.WalletResponseDto;
import ru.example.walletservice.entity.Wallet;
import ru.example.walletservice.exception.WalletNotFoundException;
import ru.example.walletservice.facade.query.WalletQueryService;
import ru.example.walletservice.mapper.WalletMapper;
import ru.example.walletservice.repository.WalletRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletQueryServiceImpl implements WalletQueryService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDto getWallet(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException(walletId.toString()));
        return walletMapper.toResponseDto(wallet);
    }
}
