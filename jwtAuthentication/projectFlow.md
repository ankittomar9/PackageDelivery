# 📚 The Complete End-to-End Architectural Deep Dive
## Microservice #1: `jwtAuthentication`

This guide explains **every single line, layer, annotation, and data transformation** that occurs inside our modernized **Spring Boot 3.4+ / Java 21** authentication microservice from the millisecond an HTTP request hits port 8084 to the database lookup and back.

---

## 🗺️ Part 1: Visual Request Flow Map

```
[ Client / IntelliJ .http ]
           │
           │  HTTP POST /login  OR  GET /validate (Authorization: Bearer <JWT>)
           ▼
[ Docker Port Bridge (-p 8084:8084) ]
           │
           ▼
[ Embedded Tomcat Web Server (Port 8084) ]
           │
           ▼
[ Spring Security Filter Chain (SecurityConfig) ]
           ├── 1. JwtRequestFilter (Intercepts Bearer Token & populates SecurityContext)
           └── 2. Authorization Rules (.requestMatchers("/login", "/validate").permitAll())
           │
           ▼
[ Spring DispatcherServlet (Front Controller) ]
           │
           ▼
[ AuthController (@RestController) ]
           │
  ┌────────┴───────────────────────────────────────────┐
  ▼                                                   ▼
POST /login                                      GET /validate
  │                                                   │
  ├── 1. AuthenticationManager.authenticate()        ├── 1. Extract Bearer Token
  │     │                                             │
  │     ▼                                             ├── 2. JwtUtil.extractUsername()
  │   DaoAuthenticationProvider                       │     (Verifies HMAC-SHA256 Signature)
  │     │                                             │
  │     ▼                                             └── 3. JwtUtil.validateToken()
  │   MyUserDetailsService.loadUserByUsername()             (Checks Expiration Date)
  │     │                                             │
  │     ▼                                             ▼
  │   UserRepository.findByUsername()            Returns AuthResponseDTO Record
  │     │                                        {"valid": true, "username": "admin"}
  │     ▼
  │   H2 In-Memory Database (myuser table)
  │     │
  │     ▼
  │   Entity (MyUser) ➔ Spring Security User
  │     │
  │     ▼
  │   PasswordEncoder (Compares Passwords)
  │     │
  │     ▼
  └── 2. JwtUtil.generateToken()
        (Signs Subject with SecretKey)
        │
        ▼
  Returns AuthResponseDTO Record
  {"jwtToken": "eyJhbG...", "valid": true}
```

---

## 🔬 Part 2: Step-by-Step Deep Dive — `POST /login` (Token Generation Flow)

When a client sends `POST http://localhost:8084/login` with payload `{"username": "admin", "password": "admin"}`:

### Step 1: The Network & Web Container Layer
1. **Docker Bridge**: Your OS routes the TCP packet sent to port `8084` through Docker Desktop's port forwarder (`0.0.0.0:8084 -> 8084/tcp`) into the container.
2. **Embedded Tomcat**: Spring Boot's embedded Tomcat server receives the raw HTTP stream, parses headers, and wraps the request in a `jakarta.servlet.http.HttpServletRequest`.

---

### Step 2: The Spring Security Filter Chain Layer
Before the request ever touches your `@RestController`, it MUST pass through Spring Security's **`SecurityFilterChain`** configured in `SecurityConfig.java`:

1. **`JwtRequestFilter`**: Extends `OncePerRequestFilter`. It checks if an `Authorization` header with `"Bearer ..."` exists. For `/login`, no token is attached yet, so `JwtRequestFilter` simply passes the request down the chain (`chain.doFilter(request, response)`).
2. **Security Rules Check**: Spring Security checks the configured `requestMatchers`:
   ```java
   .requestMatchers("/login", "/authenticate", "/validate", "/h2-console/**").permitAll()
   ```
   Since `/login` is listed under `.permitAll()`, Spring Security allows unauthenticated traffic to proceed to the controller.

---

### Step 3: The Controller Layer (`AuthController`)
The request reaches `AuthController.login(@RequestBody AuthRequestDTO authRequest)`:

1. **Jackson JSON Deserialization**: Spring Boot uses Jackson to deserialize the incoming JSON body into our Java 21 `AuthRequestDTO` record:
   ```java
   public record AuthRequestDTO(String username, String password) {}
   ```
2. **`authRequest.username()`**: Accesses `"admin"`.
3. **`authRequest.password()`**: Accesses `"admin"`.

---

### Step 4: The Authentication Subsystem (`AuthenticationManager`)
`AuthController` invokes:
```java
authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
);
```

1. **`AuthenticationManager`**: Spring Security's top-level interface for authentication.
2. **`DaoAuthenticationProvider`**: The default implementation registered in `SecurityConfig`. It delegates user retrieval to our custom `UserDetailsService`.

---

### Step 5: Service & Data Persistence Layer (`MyUserDetailsService` & `UserRepository`)

1. **`MyUserDetailsService.loadUserByUsername("admin")`**:
   Invokes `userRepository.findByUsername("admin")`.
2. **`UserRepository` (Spring Data JPA)**:
   Translates the Java method call into SQL and executes it against H2 database:
   ```sql
   SELECT user_id, username, password, token FROM myuser WHERE username = 'admin';
   ```
3. **JPA Entity Mapping**:
   Hibernate receives the DB row and populates our JPA `@Entity` object:
   ```java
   MyUser { userId="1", username="admin", password="admin", token=null }
   ```
4. **Adapter to Spring Security `UserDetails`**:
   `MyUserDetailsService` wraps `MyUser` into Spring Security's built-in `User` object:
   ```java
   return new User(myUser.getUsername(), myUser.getPassword(), Collections.emptyList());
   ```

---

### Step 6: Password Verification
1. `DaoAuthenticationProvider` retrieves the raw submitted password (`"admin"`) and compares it against the stored database password (`"admin"`) using `PasswordEncoder` (`NoOpPasswordEncoder`).
2. **If match**: Authentication succeeds!
3. **If mismatch**: Throws `BadCredentialsException`, which is caught by `GlobalExceptionHandler` to return an HTTP `401 Unauthorized` response with `ErrorResponseDTO`.

---

### Step 7: Cryptographic Token Issuance (`JwtUtil`)
Upon successful authentication, `AuthController` calls:
```java
String jwt = jwtUtil.generateToken(userDetails);
```

1. **`JwtUtil.generateToken()`**:
   Prepares claims (issued timestamp, 30-minute expiration timestamp, subject = `"admin"`).
2. **HMAC-SHA256 Signing**:
   Converts `jwt.secret` into a 256-bit `SecretKey` using `Keys.hmacShaKeyFor(...)`.
3. **Token Compaction**:
   Constructs the 3-part Base64URL string:
    * **Header**: Algorithm (`HS256` / `HS384`) & Type (`JWT`).
    * **Payload (Claims)**: `{"sub": "admin", "iat": 1786471045, "exp": 1786472845}`.
    * **Signature**: Cryptographic hash generated using the secret key.

---

### Step 8: Response Delivery
`AuthController` packages the generated token into an `AuthResponseDTO` record:
```java
return ResponseEntity.ok(new AuthResponseDTO(jwt, true));
```
Tomcat serializes this record to JSON and sends HTTP **`200 OK`**:
```json
{
  "jwtToken": "eyJhbGciOiJIUzM4NCJ9...",
  "refreshToken": null,
  "tokenType": "Bearer",
  "expiresInMs": 3600000,
  "username": null,
  "valid": true
}
```

---

## 🔬 Part 3: Step-by-Step Deep Dive — `GET /validate` (Token Verification Flow)

When another microservice (like `ComponentProcessing`) or client validates a token via `GET http://localhost:8084/validate` with header `Authorization: Bearer <TOKEN>`:

### Step 1: Interception by `JwtRequestFilter`
1. `JwtRequestFilter` intercepts the request.
2. Extracts string after `"Bearer "` prefix.
3. Invokes `jwtUtil.extractUsername(token)`.

---

### Step 2: Signature Verification & Parsing (`JwtUtil`)
1. **`Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token)`**:
    * Uses the application's `SecretKey` to recompute the cryptographic signature.
    * **If token was tampered with**: Signature check fails ➔ throws `SignatureException`.
    * **If token has passed expiration date**: Throws `ExpiredJwtException`.
    * **If signature is valid and active**: Extracts claims payload.
2. `extractUsername()` reads the `"sub"` claim ➔ returns `"admin"`.

---

### Step 3: Security Context Registration
1. `JwtRequestFilter` loads `UserDetails` from `MyUserDetailsService`.
2. Creates a `UsernamePasswordAuthenticationToken` and injects it into Spring Security's thread-local storage:
   ```java
   SecurityContextHolder.getContext().setAuthentication(authenticationToken);
   ```
   Now Spring Security considers this HTTP thread **Fully Authenticated**.

---

### Step 4: Controller Execution (`AuthController.validateToken`)
1. `AuthController.validateToken()` calls `jwtUtil.validateToken(token)`.
2. Returns **HTTP 200 OK** with:
   ```json
   {
     "jwtToken": "eyJhbG...",
     "tokenType": "Bearer",
     "expiresInMs": 1800000,
     "username": "admin",
     "valid": true
   }
   ```

---

## 📋 Part 4: Component Quick Reference (Interview Cheat Sheet)

| Class Name | Package | Role & Interview Summary |
| :--- | :--- | :--- |
| **`MyUser`** | `entity` | JPA Database Entity mapped to `myuser` table. Holds user credentials. |
| **`UserRepository`** | `repository` | Spring Data JPA Interface providing `findByUsername()` query method returning `Optional<MyUser>`. |
| **`AuthRequestDTO`** | `dto` | Immutable Java 21 `record` defining incoming `/login` payload (`username`, `password`). |
| **`AuthResponseDTO`** | `dto` | Immutable Java 21 `record` defining outgoing authentication response (`jwtToken`, `valid`, `username`). |
| **`ErrorResponseDTO`** | `dto` | Immutable Java 21 `record` defining standardized JSON error response (`statusCode`, `message`, `timestamp`). |
| **`JwtUtil`** | `security` | Cryptographic utility handling HMAC-SHA256 signing, claims extraction, and token expiration logic (JJWT 0.12.x). |
| **`JwtRequestFilter`** | `security` | `OncePerRequestFilter` intercepting HTTP `Authorization: Bearer` headers and registering authenticated users into `SecurityContextHolder`. |
| **`SecurityConfig`** | `security` | Configures Spring Security 6 `SecurityFilterChain` bean, stateless session policy, permitted routes, and `AuthenticationProvider`. |
| **`MyUserDetailsService`** | `service` | Implements Spring Security `UserDetailsService` to fetch database user entities and adapt them to Spring `UserDetails`. |
| **`GlobalExceptionHandler`**| `exception` | `@RestControllerAdvice` catching exceptions application-wide and returning clean `ErrorResponseDTO` responses. |
| **`AuthController`** | `controller` | `@RestController` exposing `/login` and `/validate` HTTP REST endpoints. |

---

Save or reference this architecture guide whenever you need to explain how the security pipeline works in your project! You now have a complete, production-grade understanding of the entire authentication microservice.