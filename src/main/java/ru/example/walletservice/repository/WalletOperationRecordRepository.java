package ru.example.walletservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.walletservice.entity.WalletOperationRecord;

import java.util.UUID;

public interface WalletOperationRecordRepository
    extends JpaRepository<WalletOperationRecord, UUID> {
}
