# Comprehensive High-Level Design (HLD) Document
## System: Return Order Management Microservices Platform

---

## 1. Executive Summary & Objective

The **Return Order Management Platform** is a distributed microservices system designed to process defective component return requests (repairs and replacements) for electronic products. 

The platform segregates concerns across 5 distinct services:
1. **`jwtAuthentication`**: Security authority issuing and validating JWTs.
2. **`ComponentProcessing`**: Core domain logic orchestrator handling return request lifecycle.
3. **`PackagingAndDelivery`**: Calculation engine for shipping and packaging costs.
4. **`PaymentService`**: Payment execution gateway for processing customer cards.
5. **`ReturnOrderPortal`**: User-facing web application client.

---

## 2. Microservice Topology & Port Mapping

| Service Name | Port | Description | Technology Stack |
| :--- | :--- | :--- | :--- |
| `jwtAuthentication` | `8084` / `8080` | User Auth & JWT Token Provider | Spring Boot 3.4+, Spring Security 6, JJWT |
| `ComponentProcessing` | `8081` | Core Return Order Orchestrator | Spring Boot 3.4+, Spring Data JPA, RestClient |
| `PackagingAndDelivery` | `8082` | Logistics & Packaging Cost Calculator | Spring Boot 3.4+, Spring Web |
| `PaymentService` | `8083` | Credit Card & Payment Gateway | Spring Boot 3.4+, Spring Data JPA |
| `ReturnOrderPortal` | `8085` | Web User Interface Client | Spring Boot 3.4+, Thymeleaf / Web Client |

---

## 3. High-Level System Architecture Diagram

```mermaid
flowchart TB
    subgraph Client Tier
        UI["ReturnOrderPortal (Port 8085)<br/>User Interface & View Controllers"]
    end

    subgraph Security & Identity Tier
        Auth["jwtAuthentication (Port 8084)<br/>Authentication Controller & JWT Service"]
    end

    subgraph Business Logic & Domain Tier
        Comp["ComponentProcessing (Port 8081)<br/>Return Request Orchestrator"]
    end

    subgraph Micro-Utility Services Tier
        Pack["PackagingAndDelivery (Port 8082)<br/>Logistics & Packaging Rules Engine"]
        Pay["PaymentService (Port 8083)<br/>Payment Processing Gateway"]
    end

    subgraph Persistence Tier
        DB1[("User Credentials DB")]
        DB2[("Process Request DB")]
        DB3[("Packaging Tariff DB")]
        DB4[("Transaction DB")]
    end

    UI -->|"1. POST /login"| Auth
    Auth -->|"Validate Credentials"| DB1
    Auth --"Return JWT"--> UI

    UI -->|"2. POST /processDetail (Bearer Token)"| Comp
    Comp -->|"Validate JWT Token"| Auth
    Comp -->|"Save Initial Request"| DB2

    Comp -->|"3. GET /getPackagingDeliveryCharge"| Pack
    Pack -->|"Lookup Rates"| DB3
    Pack --"Return Charge"--> Comp

    Comp -->|"4. POST /processPayment"| Pay
    Pay -->|"Validate Card & Save Txn"| DB4
    Pay --"Return Txn Status"--> Comp

    Comp --"Return Full Process Response"--> UI
```

---

## 4. Component Interaction & Sequence Diagrams

### 4.1 Component Return Request Lifecycle Flow

```mermaid
sequenceDiagram
    autonumber
    actor Customer
    participant Portal as ReturnOrderPortal (8085)
    participant Auth as jwtAuthentication (8084)
    participant CompProc as ComponentProcessing (8081)
    participant PackDel as PackagingAndDelivery (8082)
    participant Payment as PaymentService (8083)

    Customer->>Portal: Enters Component Details & Card Info
    Portal->>Auth: Request Bearer Token (if expired/missing)
    Auth-->>Portal: Return JWT Token

    Portal->>CompProc: POST /processDetail (Request Payload + JWT)
    
    rect rgb(240, 248, 255)
        note right of CompProc: Security Verification
        CompProc->>Auth: GET /validate (JWT Token)
        Auth-->>CompProc: 200 OK (Valid = true)
    end

    rect rgb(255, 245, 238)
        note right of CompProc: Calculate Return Duration & Charges
        alt ComponentType == "Integral"
            CompProc->>CompProc: Turnaround = 5 Days, Processing Charge = ₹500
        else ComponentType == "Accessory"
            CompProc->>CompProc: Turnaround = 2 Days, Processing Charge = ₹300
        end

        CompProc->>PackDel: GET /getPackagingDeliveryCharge?type={type}&count={count}
        PackDel-->>CompProc: Return PackagingCharge + DeliveryCharge
    end

    rect rgb(240, 255, 240)
        note right of CompProc: Payment Processing
        CompProc->>Payment: POST /processPayment (RequestId, CreditCardNo, TotalCharge)
        Payment-->>CompProc: Return { TransactionId, Status: "SUCCESS" }
    end

    CompProc-->>Portal: ProcessResponse (RequestId, ProcessingCharge, DeliveryDate, TxnId)
    Portal-->>Customer: Render Return Order Summary Page
```

---

## 5. Domain Models & Data Specifications

### 5.1 Process Request (Domain Object / DTO)
* `requestId` (Long / String - Primary Key)
* `userName` (String)
* `contactNumber` (Long / String)
* `componentType` (Enum: `INTEGRAL`, `ACCESSORY`)
* `componentName` (String)
* `quantity` (Integer)
* `isPriorityRequest` (Boolean)

### 5.2 Process Response (Domain Object / DTO)
* `requestId` (Long / String)
* `processingCharge` (Double)
* `packagingAndDeliveryCharge` (Double)
* `dateOfDelivery` (LocalDate)
* `transactionId` (String / Long)

### 5.3 Payment Details (DTO)
* `requestId` (Long / String)
* `creditCardNumber` (Long / String)
* `creditLimit` (Double)
* `processingCharge` (Double)

---

## 6. Modernization & Migration Standards (Spring Boot 3.4+ & Java 21)

### 6.1 Key Code Changes Required
1. **Jakarta EE Migration**: Replace all `javax.persistence.*`, `javax.servlet.*`, `javax.validation.*` imports with `jakarta.persistence.*`, `jakarta.servlet.*`, `jakarta.validation.*`.
2. **Spring Security 6 Configuration**: Replace legacy security configuration:
   ```java
   // LEGACY (SPRING BOOT 2.X - DEPRECATED)
   @Configuration
   public class WebSecurityConfig extends WebSecurityConfigurerAdapter { ... }

   // MODERN (SPRING BOOT 3.4+ JAVA 21)
   @Configuration
   @EnableWebSecurity
   public class SecurityConfig {
       @Bean
       public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
           return http
               .csrf(AbstractHttpConfigurer::disable)
               .authorizeHttpRequests(auth -> auth
                   .requestMatchers("/authenticate", "/h2-console/**").permitAll()
                   .anyRequest().authenticated()
               )
               .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .build();
       }
   }
   ```
3. **Java 21 Records for DTOs**: Replace mutable getter/setter boilerplate with immutability:
   ```java
   public record ProcessRequestDTO(
       String userName,
       String contactNumber,
       String componentType,
       String componentName,
       int quantity
   ) {}
   ```
4. **Declarative HTTP Client / RestClient**:
   ```java
   // Modern Spring 6 RestClient replacing RestTemplate
   RestClient restClient = RestClient.builder()
       .baseUrl("http://localhost:8082")
       .build();
   ```

---

## 7. Individual Microservice Checkpoint Documentation Strategy

We will document each microservice with its own dedicated Checkpoint Specification:
- **`CP-01-JWT-AUTH.md`**: Authentication & Token Issuer Spec
- **`CP-02-PACKAGING-DELIVERY.md`**: Packaging & Delivery Calculator Spec
- **`CP-03-PAYMENT-SERVICE.md`**: Payment Processing Spec
- **`CP-04-COMPONENT-PROCESSING.md`**: Core Orchestrator Spec
- **`CP-05-RETURN-ORDER-PORTAL.md`**: Web Portal & UI Gateway Spec

---
