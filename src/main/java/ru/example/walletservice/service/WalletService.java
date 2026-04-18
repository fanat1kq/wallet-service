package ru.example.walletservice.service;

import ru.example.walletservice.dto.request.WalletRequestDto;
import ru.example.walletservice.dto.response.WalletResponseDto;

import java.util.UUID;

public interface WalletService {

    WalletResponseDto getWallet(UUID id);

    WalletResponseDto processTransaction(UUID idempotencyKey, WalletRequestDto walletRequestDto);
}
