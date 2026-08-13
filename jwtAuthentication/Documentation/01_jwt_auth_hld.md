# High-Level Design (HLD) Document
## Microservice: `jwtAuthentication` (Authorization & Security Authority)

---

## 1. System Context & Architecture Overview

The **`jwtAuthentication`** microservice serves as the **Central Security & Identity Provider** for the Return Order Management Platform. 

In a distributed microservices environment, services cannot trust unauthenticated HTTP traffic. The `jwtAuthentication` microservice is responsible for:
1. **Authenticating User Credentials**: Validating usernames and passwords against persistent storage.
2. **Issuing Cryptographic JWT Tokens**: Creating signed, tamper-proof JSON Web Tokens (JWT) containing user identity and expiration claims.
3. **Validating Tokens for Downstream Microservices**: Verifying token signatures and expiration timestamps for services like `ComponentProcessing`, `PackagingAndDelivery`, and `PaymentService`.

---

## 2. High-Level Architecture Diagram

```mermaid
flowchart TB
    subgraph External Client Layer
        WebUI["ReturnOrderPortal / REST Client / Mobile App"]
    end

    subgraph Security Authority Microservice [jwtAuthentication : Port 8084]
        Tomcat["Embedded Tomcat Server (Port 8084)"]
        SecFilter["Spring Security FilterChain (Spring Security 6)"]
        JwtFilter["JwtRequestFilter (OncePerRequestFilter)"]
        AuthController["AuthController (@RestController)"]
        AuthMgr["AuthenticationManager & DaoAuthenticationProvider"]
        UserDetailService["MyUserDetailsService"]
        JwtCrypto["JwtUtil (JJWT 0.12.x Engine)"]
        ErrHandler["GlobalExceptionHandler (@RestControllerAdvice)"]
    end

    subgraph Data & Storage Layer
        Repo["UserRepository (Spring Data JPA)"]
        H2DB[("H2 In-Memory Database (myuser table)")]
    end

    subgraph Downstream Microservices
        CompProc["ComponentProcessing Service"]
        PackDel["PackagingAndDelivery Service"]
        Payment["PaymentService"]
    end

    WebUI -->|"1. POST /login (username, password)"| Tomcat
    Tomcat --> SecFilter
    SecFilter --> AuthController
    AuthController --> AuthMgr
    AuthMgr --> UserDetailService
    UserDetailService --> Repo
    Repo <--> H2DB
    
    AuthMgr -->|"If Password Matches"| AuthController
    AuthController --> JwtCrypto
    JwtCrypto --"Return Signed JWT Token"--> WebUI

    WebUI -->|"2. GET /validate (Bearer JWT)"| AuthController
    CompProc -->|"3. GET /validate (Bearer JWT)"| AuthController
    AuthController --> JwtCrypto
    JwtCrypto --"Validate Signature & Expiry"--> AuthController
    AuthController --"Return AuthResponseDTO (valid: true)"--> CompProc
```

---

## 3. Interaction & Sequence Flows

### 3.1 Token Issuance Sequence (`POST /login`)

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as AuthController
    participant AuthMgr as AuthenticationManager
    participant UserServ as MyUserDetailsService
    participant Repo as UserRepository
    participant DB as H2 Database
    participant Encoder as PasswordEncoder
    participant JwtUtil as JwtUtil Engine

    Client->>Controller: POST /login {username: "admin", password: "admin"}
    Controller->>AuthMgr: authenticate(UsernamePasswordAuthenticationToken)
    AuthMgr->>UserServ: loadUserByUsername("admin")
    UserServ->>Repo: findByUsername("admin")
    Repo->>DB: SELECT * FROM myuser WHERE username = 'admin'
    DB-->>Repo: Return MyUser Entity (userId, username, password)
    Repo-->>UserServ: Optional<MyUser>
    UserServ-->>AuthMgr: Return UserDetails (username, password)
    AuthMgr->>Encoder: matches(rawPassword, dbPassword)
    
    alt Passwords Match
        Encoder-->>AuthMgr: true
        AuthMgr-->>Controller: Authentication Object (Authenticated)
        Controller->>JwtUtil: generateToken(userDetails)
        JwtUtil->>JwtUtil: Build Claims + Sign with SecretKey (HMAC-SHA256)
        JwtUtil-->>Controller: Compact JWT String ("eyJhbG...")
        Controller-->>Client: HTTP 200 OK { jwtToken, valid: true }
    else Password Mismatch / User Not Found
        Encoder-->>AuthMgr: false
        AuthMgr-->>Controller: Throws BadCredentialsException
        Controller-->>Client: HTTP 401 Unauthorized { jwtToken: null, valid: false }
    end
```

### 3.2 Token Validation Sequence (`GET /validate`)

```mermaid
sequenceDiagram
    autonumber
    actor DownstreamService as Downstream Microservice / Client
    participant Controller as AuthController
    participant JwtUtil as JwtUtil Engine

    DownstreamService->>Controller: GET /validate (Header: "Authorization: Bearer <TOKEN>")
    Controller->>JwtUtil: validateToken(token)
    JwtUtil->>JwtUtil: Parse Claims using SecretKey
    
    alt Token Signature Valid & Not Expired
        JwtUtil-->>Controller: true
        Controller->>JwtUtil: extractUsername(token)
        JwtUtil-->>Controller: "admin"
        Controller-->>DownstreamService: HTTP 200 OK { valid: true, username: "admin" }
    else Signature Invalid OR Token Expired
        JwtUtil-->>Controller: false
        Controller-->>DownstreamService: HTTP 401 Unauthorized { valid: false, username: null }
    end
```

---

## 4. Modernization Baseline (Spring Boot 3.4+ & Java 21)

| Dimension | Legacy Standard (Spring Boot 2.x) | Modern Production Standard (Our App) |
| :--- | :--- | :--- |
| **Java Baseline** | Java 8 / 11 | **Java 21 LTS** (Virtual Threads, Records) |
| **Servlet Namespace** | `javax.servlet.*` | **`jakarta.servlet.*`** |
| **Persistence Namespace**| `javax.persistence.*` | **`jakarta.persistence.*`** |
| **Spring Security** | `WebSecurityConfigurerAdapter` (Abstract Class) | **`SecurityFilterChain` Bean Injection** |
| **JWT Library** | `jjwt 0.9.1` (Deprecation & JAXB Crashes) | **`jjwt 0.12.6`** (Modular, SecretKey Enforcement) |
| **Data Objects** | Mutable Classes with Getters/Setters | **Java 21 `record` types** (`AuthRequestDTO`, `AuthResponseDTO`) |
| **API Documentation** | Springfox / Swagger 2 | **SpringDoc OpenAPI 3** (`/swagger-ui.html`) |

---

## 5. Deployment Topology (Docker Containerization)

```
┌──────────────────────────────────────────────────────────┐
│ HOST MACHINE (Windows / Linux / Cloud OS)                 │
│                                                          │
│  IntelliJ / Postman / Microservices                      │
│  http://localhost:8084 or http://127.0.0.1:8084          │
│           │                                              │
│           ▼                                              │
│  Docker Host Port Binding (-p 8084:8084)                 │
│           │                                              │
└───────────┼──────────────────────────────────────────────┘
            ▼
┌──────────────────────────────────────────────────────────┐
│ DOCKER CONTAINER (jwt-auth-container)                    │
│ Base Image: eclipse-temurin:21-jre-alpine                │
│                                                          │
│  ├── /app/app.jar (Compiled Spring Boot Application)     │
│  ├── Non-root User (appuser:appgroup)                    │
│  └── Internal Port: 8084                                 │
└──────────────────────────────────────────────────────────┘
```
