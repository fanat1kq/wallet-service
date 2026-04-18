package ru.example.walletservice.util;

public final class ErrorMessages {

    public static final String MESSAGE_MAX_BALANCE_EXCEEDED = ("Баланс не может превышать %s");

    public static final String MESSAGE_INSUFFICIENT_FUNDS =
        ("Недостаточно средств на счёте с ID %s, доступно %s");

    public static final String MESSAGE_OPERATION_TYPE_NOT_FOUND =
        ("Не удалось определить тип операции");

    public static final String MESSAGE_WALLET_NOT_FOUND = ("Кошелёк с ID %s не найден");

    private ErrorMessages() {
    }
}