package ru.example.walletservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.UUID;

public class NotZeroUuidValidator implements ConstraintValidator<NotZeroUuid, UUID> {

    private static final UUID ZERO_UUID = new UUID(0L, 0L);

    @Override
    public boolean isValid(UUID value, ConstraintValidatorContext context) {
        return value != null && !ZERO_UUID.equals(value);
    }
}
