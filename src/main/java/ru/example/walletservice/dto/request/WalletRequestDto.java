package ru.example.walletservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import ru.example.walletservice.dto.enumerations.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletRequestDto(
    @NotNull(message = "Не введён идентификатор счёта")
    UUID walletId,

    @NotNull(message = "Не выбран тип операции")
    OperationType operationType,

    @NotNull(message = "Не введена сумма")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше нуля")
    @Digits(integer = 13, fraction = 2,
        message = "Сумма должна иметь не более 13 цифр в целой части и не более 2 в дробной части")
    BigDecimal amount
) {
}
