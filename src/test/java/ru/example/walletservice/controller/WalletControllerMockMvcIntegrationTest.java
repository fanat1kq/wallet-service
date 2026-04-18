package ru.example.walletservice.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.example.walletservice.dto.enumerations.OperationType;
import ru.example.walletservice.dto.request.WalletRequestDto;
import ru.example.walletservice.entity.Wallet;
import ru.example.walletservice.repository.WalletRepository;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class WalletControllerMockMvcIntegrationTest extends BaseIntegrationTest {

    private static final BigDecimal MAX_REQUEST_AMOUNT = new BigDecimal("9999999999999.99");
    private final PodamFactory podamFactory = new PodamFactoryImpl();

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Wallet wallet;

    private static BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(new BigDecimal("0.01")) < 0) {
            return new BigDecimal("10.00");
        }
        BigDecimal scaled = amount.abs().setScale(2, RoundingMode.HALF_UP);
        if (scaled.compareTo(MAX_REQUEST_AMOUNT) > 0) {
            return new BigDecimal("10.00");
        }
        return scaled;
    }

    @BeforeEach
    void setup() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            """
                INSERT INTO wallets (id, balance, created_at, modified_at)
                VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
            id, new BigDecimal("0.01"));
        wallet = walletRepository.findById(id).orElseThrow();
    }

    @AfterEach
    void deleteTestWallet() {
        jdbcTemplate.update("DELETE FROM wallet_operations WHERE wallet_id = ?", wallet.getId());
        jdbcTemplate.update("DELETE FROM wallets WHERE id = ?", wallet.getId());
    }

    private WalletRequestDto randomValidWalletRequest(UUID walletId, OperationType operationType) {
        WalletRequestDto seed = podamFactory.manufacturePojo(WalletRequestDto.class);
        return new WalletRequestDto(walletId, operationType, normalizeAmount(seed.amount()));
    }

    @Test
    @DisplayName("GET /wallets/{id}: existing wallet returns 200 with wallet data")
    void givenId_whenGetExistingWallet_thenStatus200andWalletReturned() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/{walletId}", wallet.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.walletId").value(wallet.getId().toString()))
            .andExpect(jsonPath("$.balance").value(wallet.getBalance()));
    }

    @Test
    @DisplayName("GET /wallets/{id}: unknown wallet id returns 404 with detail")
    void givenId_whenGetNonExistingWallet_thenStatus404andExceptionThrown() throws Exception {
        UUID randomWalletId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/wallets/{walletId}", randomWalletId.toString()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail")
                .value(String.format("Кошелёк с ID %s не найден", randomWalletId)));
    }

    @Test
    @DisplayName("POST /wallet: withdraw from non-existent wallet returns 404")
    void givenRequest_whenWithdrawFromNonExistentWallet_thenStatus404andExceptionThrown()
        throws Exception {
        UUID randomWalletId = UUID.randomUUID();
        WalletRequestDto walletRequestDto =
            randomValidWalletRequest(randomWalletId, OperationType.WITHDRAW);

        mockMvc.perform(post("/api/v1/wallet")
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(walletRequestDto)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail")
                .value(String.format("Кошелёк с ID %s не найден", randomWalletId)));
    }

    @Test
    @DisplayName("POST /wallet: withdraw with insufficient funds returns 400")
    void givenRequest_whenInsufficientFundsForWithdraw_thenStatus400andExceptionThrown()
        throws Exception {
        WalletRequestDto walletRequestDto = new WalletRequestDto(
            wallet.getId(),
            OperationType.WITHDRAW,
            new BigDecimal("1000")
        );

        mockMvc.perform(post("/api/v1/wallet")
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(walletRequestDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value(
                String.format("Недостаточно средств на счёте с ID %s, доступно %s",
                    wallet.getId(),
                    wallet.getBalance())));
    }

    @Test
    @DisplayName("GET /wallets/{id}: invalid wallet id format returns 422")
    void givenId_whenIncorrectIdProvided_thenStatus422andExceptionThrown() throws Exception {
        String incorrectId = "id-id";

        mockMvc.perform(get("/api/v1/wallets/{walletId}", incorrectId))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value("Некорректный формат параметра: walletId"));
    }

    @Test
    @DisplayName("POST /wallet: missing operation type returns 422")
    void givenRequest_whenIncorrectOperationTypeProvided_thenStatus422andExceptionThrown()
        throws Exception {
        WalletRequestDto walletRequestDto = new WalletRequestDto(
            wallet.getId(),
            null,
            normalizeAmount(podamFactory.manufacturePojo(BigDecimal.class))
        );

        mockMvc.perform(post("/api/v1/wallet")
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(walletRequestDto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value("operationType: Не выбран тип операции"));
    }

    @Test
    @DisplayName("POST /wallet: zero amount returns 422")
    void givenRequest_whenZeroAmountProvided_thenStatus422andExceptionThrown() throws Exception {
        WalletRequestDto seed = podamFactory.manufacturePojo(WalletRequestDto.class);
        WalletRequestDto walletRequestDto = new WalletRequestDto(
            wallet.getId(),
            seed.operationType(),
            BigDecimal.ZERO
        );

        mockMvc.perform(post("/api/v1/wallet")
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(walletRequestDto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value("amount: Сумма должна быть больше нуля"));
    }

    @Test
    @DisplayName("POST /wallet: zero UUID Idempotency-Key returns 422")
    void givenZeroUuidIdempotencyKey_whenPostWallet_thenStatus422() throws Exception {
        WalletRequestDto walletRequestDto =
            randomValidWalletRequest(wallet.getId(), OperationType.DEPOSIT);

        mockMvc.perform(post("/api/v1/wallet")
                .header("Idempotency-Key", "00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(walletRequestDto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value(
                "updateBalance.idempotencyKey: Idempotency-Key не может быть нулевым UUID"));
    }

    @Test
    @DisplayName("POST /wallet: missing Idempotency-Key header returns 422")
    void givenMissingIdempotencyKey_whenPostWallet_thenStatus422() throws Exception {
        WalletRequestDto walletRequestDto =
            randomValidWalletRequest(wallet.getId(), OperationType.DEPOSIT);

        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(walletRequestDto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(
                jsonPath("$.detail").value("Отсутствует обязательный заголовок: Idempotency-Key"));
    }
}