# Wallet Service

REST API для работы со счетами: просмотр баланса, операции пополнения и снятия средств с **обязательным заголовком
идемпотентности** `Idempotency-Key` (повтор одного и того же запроса не применяет операцию дважды; конфликт при том же
ключе и другом теле — ответ `409`).

## Что сделано в проекте

- **HTTP API**: `GET` баланса по `walletId`, `POST` операции `DEPOSIT` / `WITHDRAW` с валидацией суммы и типа операции.
- **Идемпотентность**: хранение обработанных операций (`wallet_operations`), защита от повторного списания/зачисления по
  одному ключу.
- **Ошибки**: ответы в формате [Problem Details](https://www.rfc-editor.org/rfc/rfc9457.html) (`title`, `detail`,
  `status`, `errorCode`, при валидации — `errors`).
- **Персистентность**: PostgreSQL, схема и данные через **Liquibase** (создание таблиц, начальные счета, аудит по
  времени).
- **Документация API**: **SpringDoc OpenAPI** (Swagger UI).
- **Мониторинг**: **Spring Boot Actuator** (`health`, `info`).
- **Тесты**: интеграционные сценарии (`MockMvc`), **Testcontainers** (PostgreSQL), при необходимости — фикстуры через *
  *Podam**.
  - **История транзакций**: добавлена сущность WalletOperationRecord для сохранения истории транзакций.

Базовый путь API задаётся свойством `api-base-path` (по умолчанию **`/api/v1`** через `API_VERSION`).

## Технологии

| Компонент        | Версия / стек     |
|------------------|-------------------|
| Java             | 17                |
| Spring Boot      | 3.5.x             |
| PostgreSQL       | 15                |
| Liquibase        | из BOM Spring     |
| Docker / Compose | см. репозиторий   |
| Сборка           | Maven             |
| Маппинг DTO      | MapStruct         |
| Документация     | springdoc-openapi |

## Требования

- **Docker** и **Docker Compose** (для запуска «всё в контейнерах»).
- Либо **JDK 17**, локальный или удалённый **PostgreSQL** (для запуска без Docker только приложения).

## Быстрый старт (Docker Compose)

Рабочая директория — каталог `wallet-service` (где лежат `pom.xml`, `Dockerfile`, `docker-compose.yml`).

1. Скопируйте пример окружения и при необходимости поправьте значения:

   ```bash
   cp .env.example .env
   ```

2. Соберите и поднимите сервисы:

   ```bash
   docker compose up --build
   ```

3. Приложение слушает **порт `8080`**. PostgreSQL с хоста доступен на **`localhost:5433`** (маппинг `5433:5432`).

4. Остановка без удаления тома БД:

   ```bash
   docker compose stop
   ```

   Полная остановка с удалением контейнеров и тома данных:

   ```bash
   docker compose down -v
   ```

В `docker-compose` для приложения передаётся `SPRING_DATASOURCE_*`; локальные переменные из `.env` в контейнер
приложения попадают через `env_file` — убедитесь, что пароль пользователя БД совпадает с `POSTGRES_PASSWORD`.

## Локальный запуск (без Docker для приложения)

1. Поднимите PostgreSQL (порт по умолчанию в примере — `5432`, база `wallet_db`, пользователь/пароль из `.env.example`).

2. Скопируйте окружение:

   ```bash
   cp .env.example .env
   ```

   При необходимости Spring Boot подхватит `optional:file:.env.properties` — для простоты можно экспортировать
   переменные из `.env` в shell или использовать плагин/обёртку под вашу ОС.

3. Сборка и тесты:

   ```bash
   mvn clean verify
   ```

4. Запуск:

   ```bash
   mvn spring-boot:run
   ```

## Переменные окружения

Полный перечень и значения по умолчанию — в **`.env.example`**. Ключевые:

| Переменная                             | Назначение                                                       |
|----------------------------------------|------------------------------------------------------------------|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Подключение JDBC к PostgreSQL                                    |
| `SERVER_PORT`                          | Порт HTTP (по умолчанию `8080`)                                  |
| `API_VERSION`                          | Сегмент версии в пути: `/api/${API_VERSION}` → по умолчанию `v1` |

**Важно:** в `application.yml` нельзя использовать опечатку вида `${API_VERSION}}` (две закрывающие скобки) — в путь
попадёт лишняя `}` и приложение не стартует.

## Документация и служебные URL

| Назначание        | URL (при `SERVER_PORT=8080` и версии `v1`) |
|-------------------|--------------------------------------------|
| Swagger UI        | http://localhost:8080/swagger-ui.html      |
| OpenAPI JSON      | http://localhost:8080/v3/api-docs          |
| Health (Actuator) | http://localhost:8080/actuator/health      |
| Info (Actuator)   | http://localhost:8080/actuator/info        |

## Примеры запросов

Базовый префикс: **`/api/v1`** (если не меняли `API_VERSION`).

### Получить счёт

```http
GET /api/v1/wallets/{walletId}
```

### Операция по счёту

Обязательный заголовок: **`Idempotency-Key: <UUID>`** (не нулевой UUID).

```http
POST /api/v1/wallet
Content-Type: application/json
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
```

Тело (JSON):

```json
{
  "walletId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
  "operationType": "DEPOSIT",
  "amount": "1000.00"
}
```

`operationType`: `DEPOSIT` или `WITHDRAW`. Сумма — не меньше `0.01`, не более 13 знаков в целой части и 2 в дробной (см.
валидацию в `WalletRequestDto`).

## Начальные данные в БД

В проекте **нет отдельного HTTP-эндпоинта для создания счёта**. Тестовые/начальные записи в таблицу `wallets`
накатываются Liquibase:

- файл сценария: `src/main/resources/db/changelog/v0.0.1/ddl/001-insert-data-wallets.xml` (подключён из
  `changelog-master.xml`).

После миграций можно вызывать API для существующих `walletId` из этого набора (UUID смотрите в changelog).

## Структура репозитория (кратко)

```
wallet-service/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── .env.example
└── src/
    ├── main/java/ru/example/walletservice/   # приложение
    └── main/resources/
        ├── application.yml
        └── db/changelog/                       # Liquibase
```

## Полезные команды Maven

```bash
mvn test                    # только тесты
mvn -DskipTests package     # JAR без тестов
```

---

При проблемах со стартом включите отладку контекста: `java -jar ... --debug` или переменная
`LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_BOOT=DEBUG`.
