# 25+ Comprehensive Interview Questions & Answers
## Subject: Spring Boot 3.4+, Java 21, Security & Microservices Architecture

---

### Q1: What are the main breaking changes when migrating from Spring Boot 2.x to Spring Boot 3.x / 4.x?
**Answer**:
1. **Baseline Java Version**: Spring Boot 3 requires Java 17 minimum (Java 21 LTS recommended).
2. **Jakarta EE Namespace Migration**: All `javax.*` packages (JPA, Servlet, Validation) were renamed to `jakarta.*` (e.g. `jakarta.persistence.*`, `jakarta.servlet.*`).
3. **Spring Security 6 Overhaul**: Removal of `WebSecurityConfigurerAdapter`, removal of `.antMatchers()` (replaced by `.requestMatchers()`), and deprecation of chained fluent configuration in favor of Lambda DSL.
4. **Data Initializer Deferral**: `spring.jpa.defer-datasource-initialization=true` is required in Spring Boot 3 so Hibernate generates DDL schemas before `data.sql` executes.

---

### Q2: Why did Java replace `javax.*` with `jakarta.*`?
**Answer**:
Oracle transferred Java EE governance to the Eclipse Foundation under the new name **Jakarta EE**. Oracle retained trademark ownership of the `javax` package namespace, requiring Eclipse to transition all APIs to the `jakarta.*` package namespace starting in Jakarta EE 9.

---

### Q3: What is a Java 21 `record` and why should it be used for DTOs?
**Answer**:
A Java `record` (introduced in Java 14, finalized in 16/17/21) is an immutable data carrier. It automatically generates getters, `equals()`, `hashCode()`, and `toString()`. 
Using `record` for DTOs enforces immutability, thread-safety, eliminates Lombok/getter boilerplate, and prevents accidental payload mutation across HTTP request threads.

---

### Q4: How has `WebSecurityConfigurerAdapter` been replaced in Spring Security 6?
**Answer**:
`WebSecurityConfigurerAdapter` was deprecated in Spring Security 5.7 and removed in Spring Security 6. It was replaced by registering a `@Bean` of type `SecurityFilterChain` that takes `HttpSecurity` as a parameter:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.requestMatchers("/login").permitAll().anyRequest().authenticated())
        .build();
}
```

---

### Q5: What is the difference between `antMatchers()` and `requestMatchers()` in Spring Security?
**Answer**:
`antMatchers()` was removed in Spring Security 6 because it had subtle path-matching discrepancies with Spring MVC path patterns, potentially creating security authorization bypasses. `requestMatchers()` uses Spring's `MvcRequestMatcher` by default, providing unified path matching between Security filters and Spring MVC controllers.

---

### Q6: How does JWT Token Authentication work in a Stateless Microservices Architecture?
**Answer**:
1. **Authentication**: The client sends username/password to `/login`.
2. **Token Generation**: The server verifies credentials against the database and signs a JWT containing claims (`sub`, `exp`, `iat`) using a secret key (HMAC-SHA256).
3. **Stateless Transmission**: The server returns the JWT. It does NOT store session state in server memory (`SessionCreationPolicy.STATELESS`).
4. **Validation**: The client attaches `Authorization: Bearer <TOKEN>` to subsequent requests. Downstream microservices verify the signature using the shared secret key without querying a session store.

---

### Q7: Why did we upgrade from `jjwt 0.9.1` to `jjwt 0.12.6`?
**Answer**:
1. **JDK 17/21 Compatibility**: JJWT 0.9.1 relied on `javax.xml.bind.DatatypeConverter` (JAXB), which was removed in modern JDKs, causing `NoClassDefFoundError` at runtime.
2. **Type Safety & Key Entropy Enforcement**: JJWT 0.12.x enforces a minimum 256-bit key length for HMAC-SHA256 (`Keys.hmacShaKeyFor()`), throwing a `WeakKeyException` if weak secret keys are provided.
3. **Modern API**: Replaced `Jwts.parser().setSigningKey()` with type-safe `Jwts.parser().verifyWith(SecretKey).build().parseSignedClaims(token)`.

---

### Q8: What is the role of `OncePerRequestFilter` in Spring Security?
**Answer**:
`OncePerRequestFilter` is a base class that guarantees a filter executes exactly once per HTTP request dispatch. We extend it in `JwtRequestFilter` to intercept headers, extract JWT tokens, validate signatures, and register authenticated users in `SecurityContextHolder.getContext().setAuthentication()`.

---

### Q9: Why do we return `Optional<MyUser>` from Spring Data JPA Repositories?
**Answer**:
Returning `Optional<T>` explicitly signals that a database record may be absent. It eliminates `null` checks and prevents `NullPointerException` by forcing clean functional handling:
```java
userRepository.findByUsername(username)
    .orElseThrow(() -> new UsernameNotFoundException("User missing"));
```

---

### Q10: Why did we use Constructor Injection (`@RequiredArgsConstructor`) instead of `@Autowired` on fields?
**Answer**:
1. **Immutability**: Allows fields to be declared `private final`.
2. **Testability**: Dependencies can be injected manually in unit tests without Spring or reflection.
3. **Circular Dependencies**: Detected at compile/startup time rather than runtime.
4. **Spring Recommendation**: Spring framework officially recommends constructor injection over field injection.

---

### Q11: What is the purpose of `@RestControllerAdvice`?
**Answer**:
`@RestControllerAdvice` is a specialized Spring component that intercepts exceptions thrown application-wide across all `@RestController` classes. Combined with `@ExceptionHandler`, it converts raw exceptions (like `BadCredentialsException` or `ExpiredJwtException`) into standardized HTTP error responses (`ErrorResponseDTO`).

---

### Q12: Explain the structure of a JSON Web Token (JWT).
**Answer**:
A JWT consists of 3 Base64URL-encoded strings separated by dots (`.`):
1. **Header**: Contains algorithm type (`HS256`, `HS384`) and token type (`JWT`).
2. **Payload**: Contains claims (subject `sub`, issue date `iat`, expiration date `exp`, roles).
3. **Signature**: Generated by hashing `Header + Payload` with a Secret Key: `HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)`.

---

### Q13: What is the difference between `@Mock` and `@InjectMocks` in Mockito?
**Answer**:
* `@Mock`: Creates a fake, dummy instance of a class/interface.
* `@InjectMocks`: Creates a real instance of a class and automatically injects all created `@Mock` objects into its constructor/fields.

---

### Q14: What is the difference between `@Mock` and `@MockitoBean` in Spring Boot 3.4+?
**Answer**:
* `@Mock`: Pure Mockito annotation for standalone Java unit tests (no Spring context).
* `@MockitoBean`: Introduced in Spring Boot 3.4 (replacing deprecated `@MockBean`) to register a Mockito mock inside Spring's test `ApplicationContext` during `@WebMvcTest` or `@SpringBootTest`.

---

### Q15: What is `MockMvc` and why do we use it in Controller testing?
**Answer**:
`MockMvc` is a main entry point for Spring MVC testing that allows sending HTTP requests (`post()`, `get()`) and asserting response status (`status().isOk()`) and JSON content (`jsonPath()`) without launching a full embedded HTTP web server.

---

### Q16: Why did we use Multi-Stage Builds in Docker (`Dockerfile`)?
**Answer**:
Multi-stage builds separate the **Build Environment** (`maven:3.9-eclipse-temurin-21-alpine`) from the **Runtime Environment** (`eclipse-temurin:21-jre-alpine`). This prevents source code, Maven plugins, and build compilers from ending up in the final image, reducing image size from ~800MB to ~160MB and improving container security.

---

### Q17: Why did we add `USER appuser` in the Dockerfile?
**Answer**:
By default, Docker containers run as the Linux `root` user. If an attacker exploits a vulnerability in the application, root privileges inside the container could allow host-level compromise. Running as a non-root user (`USER appuser`) enforces security least-privilege principles.

---

### Q18: What does `-p 8084:8084` do when running a Docker container?
**Answer**:
It maps **Host Port 8084** to **Container Port 8084**. `EXPOSE 8084` inside the Dockerfile only documents internal port usage; `-p 8084:8084` opens the network bridge allowing localhost traffic from Windows to reach the container.

---

### 19: Why use `127.0.0.1` instead of `localhost` when testing Docker containers on Windows?
**Answer**:
On Windows OS, `localhost` resolves to both IPv6 (`[::1]`) and IPv4 (`127.0.0.1`). Docker Desktop binds ports on IPv4 by default. Connecting to `127.0.0.1` forces an IPv4 socket connection, bypassing IPv6 connection timeouts.

---

### Q20: What is OpenAPI 3 / Swagger UI and how is it integrated in Spring Boot 3?
**Answer**:
OpenAPI 3 is an open specification for RESTful API documentation. In Spring Boot 3, it is integrated using `springdoc-openapi-starter-webmvc-ui`. It scans `@RestController`, `@Operation`, and `@ApiResponse` annotations to generate an interactive HTML documentation portal at `/swagger-ui.html`.

---

### Q21: Why does Spring Security block Swagger UI by default?
**Answer**:
Swagger UI serves assets at `/swagger-ui.html`, `/swagger-ui/**`, and `/v3/api-docs/**`. Because Spring Security defaults to denying unauthenticated access to unlisted endpoints, these paths return HTTP 403 Forbidden until explicitly permitted via `.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()`.

---

### Q22: What is the difference between `Authentication` and `Authorization`?
**Answer**:
* **Authentication**: Verifies *who* the user is (e.g. validating username & password at `/login`).
* **Authorization**: Verifies *what* the authenticated user is allowed to do (e.g. checking if a user has `ROLE_ADMIN` to access `/delete`).

---

### Q23: Why did we set `SessionCreationPolicy.STATELESS` in `SecurityConfig`?
**Answer**:
In RESTful microservices, state should not be stored in server memory (`HttpSession`). Setting `SessionCreationPolicy.STATELESS` instructs Spring Security to never create or use HTTP sessions to store security contexts between requests. Every request must be authenticated independently via JWT.

---

### Q24: What is `NoOpPasswordEncoder` and why is `BCryptPasswordEncoder` preferred in Production?
**Answer**:
`NoOpPasswordEncoder` performs plain-text password comparison without hashing. It is used only for rapid testing or legacy migration. `BCryptPasswordEncoder` uses a strong cryptographic hashing algorithm with salting and work factor parameters to prevent rainbow table attacks.

---

### Q25: How does Spring Data JPA physical naming strategy handle Java field names?
**Answer**:
Spring Boot's default Physical Naming Strategy transforms Java camelCase property names (e.g. `userId`) into SQL snake_case column names (e.g. `user_id`). Explicitly declaring `@Column(name = "user_id")` ensures entity mappings match SQL table DDL exactly.
