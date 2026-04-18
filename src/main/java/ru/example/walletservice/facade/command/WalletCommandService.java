package ru.example.walletservice.facade.command;

import ru.example.walletservice.dto.request.WalletRequestDto;
import ru.example.walletservice.dto.response.WalletResponseDto;

import java.util.UUID;


public interface WalletCommandService {

    WalletResponseDto processTransaction(UUID idempotencyKey, WalletRequestDto walletRequestDto);
}
