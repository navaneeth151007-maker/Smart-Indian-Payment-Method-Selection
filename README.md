# IndiaPaymentRouter

A Spring Boot application that automatically routes a fund transfer to **UPI**, **IMPS**,
or **NEFT** based on the transfer amount. It exposes both a **REST API** and an
**interactive console menu** (via `CommandLineRunner`), and keeps everything in memory —
there is no database.

---

## 1. Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.4 |
| Build | Maven |
| Data carriers | Java `record`s (DTOs) |
| Extras | Spring Boot DevTools (hot reload) |

No Lombok, no Bean Validation starter, no Swagger — validation and JSON field
naming are handled manually in code (see below).

---

## 2. Project Structure

```
IndiaPaymentRouter/
├── pom.xml
├── .mvn/wrapper/maven-wrapper.properties
└── src/main/
    ├── resources/application.properties
    └── java/com/paymentrouter/
        ├── IndiaPaymentRouterApplication.java   (main class + console menu)
        ├── controller/  TransferController.java
        ├── service/     TransferService.java
        ├── dto/         TransferRequest.java, ReceiverDetails.java, TransferResponse.java
        └── exception/   InvalidAmountException.java, InvalidSenderDetailsException.java,
                          InvalidReceiverDetailsException.java, SameAccountTransferException.java,
                          GlobalExceptionHandler.java, ErrorResponse.java
```

---

## 3. Payment Method Selection Rule

Implemented in `TransferService.resolvePaymentMethod(...)` using a Java 21 pattern-matching
`switch` on `BigDecimal`:

| Amount | Method |
|---|---|
| `≤ ₹1,00,000` | **UPI** |
| `> ₹1,00,000` and `≤ ₹2,00,000` | **IMPS** |
| `> ₹2,00,000` | **NEFT** |

> Note: unlike some payment-routing designs, this version does **not** check whether
> sender and receiver banks match — the rule is amount-only. The thresholds
> (`UPI_LIMIT`, `IMPS_LIMIT`) are constants inside `TransferService`; adjust them there
> if your business rule differs.

---

## 4. Validation Rules

Performed manually in `TransferService` (no `@Valid` / Bean Validation annotations):

- **Sender name** / **Receiver name** — letters and spaces only (`[a-zA-Z ]+`)
- **Sender account** / **Receiver account** — exactly 10 digits (`\d{10}`)
- **Sender bank** / **Receiver bank** — must not be blank
- **Amount** — must be greater than 0
- **Sender account ≠ Receiver account** — rejected if identical

Each failure throws a dedicated exception, caught by `GlobalExceptionHandler`
and converted into a uniform `ErrorResponse`:

```json
{
  "timestamp": "2026-07-14T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Sender account number must contain exactly 10 digits",
  "path": "uri=/api/transfer"
}
```

---

## 5. REST API

```
POST http://localhost:8082/api/transfer
Content-Type: application/json
```

### Request (note the snake_case field names)
```json
{
  "sender_name": "Aarav Sharma",
  "acc_no": "1234567890",
  "bank_name": "HDFC Bank",
  "amount": 75000,
  "receiver": {
    "receiver_name": "Rahul Verma",
    "acc_no": "9988776655",
    "bank_name": "ICICI Bank"
  }
}
```

### Response — 200 OK
```json
{
  "receiver_name": "Rahul Verma",
  "receiver_acc_no": "9988776655",
  "amount": 75000,
  "method": "UPI"
}
```

### Sample: NEFT (amount > ₹2,00,000)
```json
{
  "sender_name": "Aarav Sharma",
  "acc_no": "1234567890",
  "bank_name": "HDFC Bank",
  "amount": 250000,
  "receiver": {
    "receiver_name": "Priya Nair",
    "acc_no": "5566778899",
    "bank_name": "HDFC Bank"
  }
}
```
```json
{
  "receiver_name": "Priya Nair",
  "receiver_acc_no": "5566778899",
  "amount": 250000,
  "method": "NEFT"
}
```

---

## 6. Interactive Console Menu

On startup, a `CommandLineRunner` bean also launches a text menu in the same terminal
the app is running in — useful for quick manual testing without Postman:

```
================================
 IndiaPaymentRouter - Main Menu
================================
 1. New Transfer
 2. View Transfer History
 3. Exit
--------------------------------
Choose an option:
```

- **Option 1** prompts for sender/receiver details line by line and prints the routed method.
- **Option 2** lists every transfer made in the current session (in-memory list, cleared on restart).
- **Option 3** exits the console loop only — the REST API keeps running on port 8082 until you
  stop the process (Ctrl+C).

---

## 7. Running the Project

**Prerequisites**: JDK 21, Maven 3.9+ (or use the bundled wrapper if `mvnw`/`mvnw.cmd`
scripts are present alongside `.mvn/wrapper/maven-wrapper.properties`).

```bash
cd IndiaPaymentRouter
mvn clean install
mvn spring-boot:run
```

The app starts on **port 8082**:
- REST endpoint: `POST http://localhost:8082/api/transfer`
- Console menu: appears directly in the same terminal

Run tests:
```bash
mvn test
```

---

## 8. Known Limitations / Possible Extensions

- No same-bank check for IMPS vs NEFT — purely amount-based today.
- No Swagger/OpenAPI UI configured (unlike some other routing services) — test via
  the console menu or Postman/curl.
- Transfer history is in-memory per session only (lost on restart) and only visible
  through the console menu, not the REST API.
- Thresholds are hard-coded constants in `TransferService`; externalizing them to
  `application.properties` would make them configurable per environment.
