package ru.example.walletservice.dto.response;

import java.math.BigDecimal;

public record WalletResponseDto(
    String walletId,
    BigDecimal balance) {
}
