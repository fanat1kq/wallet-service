package ru.example.walletservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.example.walletservice.dto.request.WalletRequestDto;
import ru.example.walletservice.dto.response.WalletResponseDto;
import ru.example.walletservice.service.WalletService;
import ru.example.walletservice.validation.NotZeroUuid;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("${api-base-path}")
@Tag(name = "Счета", description = "Управление счетами")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("wallets/{walletId}")
    @Operation(summary = "Получение информации о счёте",
        description = "Позволяет получить информацию о счёте по его идентификатору")
    public WalletResponseDto getWalletInfo(@PathVariable UUID walletId) {
        return walletService.getWallet(walletId);
    }

    @PostMapping("/wallet")
    @Operation(summary = "Обновление счёта",
        description = "Позволяет выполнить операцию на счётом (пополнение, снятие средств)")
    public WalletResponseDto updateBalance(
        @NotNull(message = "Не передан Idempotency-Key")
        @NotZeroUuid(message = "Idempotency-Key не может быть нулевым UUID")
        @RequestHeader("Idempotency-Key") UUID idempotencyKey,
        @Valid @RequestBody WalletRequestDto walletRequestDto) {
        return walletService.processTransaction(idempotencyKey, walletRequestDto);
    }


}