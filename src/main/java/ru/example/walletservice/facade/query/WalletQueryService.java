package ru.example.walletservice.facade.query;

import ru.example.walletservice.dto.response.WalletResponseDto;

import java.util.UUID;

public interface WalletQueryService {

    WalletResponseDto getWallet(UUID walletId);
}
