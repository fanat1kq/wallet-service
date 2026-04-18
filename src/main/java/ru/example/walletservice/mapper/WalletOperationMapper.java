package ru.example.walletservice.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.example.walletservice.dto.request.WalletRequestDto;
import ru.example.walletservice.entity.WalletOperationRecord;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    builder = @Builder(disableBuilder = true)
)
public interface WalletOperationMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    WalletOperationRecord toEntity(UUID operationId, WalletRequestDto dto,
                                   BigDecimal resultingBalance);
}