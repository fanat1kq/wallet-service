package ru.example.walletservice.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.example.walletservice.dto.response.WalletResponseDto;
import ru.example.walletservice.entity.Wallet;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WalletMapper {

    @Mapping(target = "walletId", source = "id")
    WalletResponseDto toResponseDto(Wallet wallet);
}