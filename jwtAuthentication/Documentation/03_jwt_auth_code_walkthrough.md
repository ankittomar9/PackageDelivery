# Code Walkthrough & Line-by-Line Technical Explanation
## Microservice: `jwtAuthentication`

---

## 1. Data Transfer Objects (`dto` Package)

### 1.1 `AuthRequestDTO.java` (Java 21 Record)
```java
package com.company.authorizationservice.dto;

public record AuthRequestDTO(
    String username,
    String password
) {}
```
* **Why Java 21 `record`?**  
  Records are immutable data carriers. Once instantiated during JSON deserialization, the fields `username` and `password` cannot be modified. It automatically provides `username()`, `password()`, `equals()`, `hashCode()`, and `toString()`.

---

### 1.2 `AuthResponseDTO.java` (Java 21 Record)
```java
package com.company.authorizationservice.dto;

public record AuthResponseDTO(
    String jwtToken,
    String refreshToken,
    String tokenType,
    Long expiresInMs,
    String username,
    Boolean valid
) {
    public AuthResponseDTO(String jwtToken, Boolean valid) {
        this(jwtToken, null, "Bearer", 3600000L, null, valid);
    }
}
```
* **Custom Compact Constructor**: `public AuthResponseDTO(String jwtToken, Boolean valid)` provides backward compatibility for simple 2-parameter initialization while setting default values (`tokenType = "Bearer"`, `expiresInMs = 3600000L`).

---

### 1.3 `ErrorResponseDTO.java` (Java 21 Record)
```java
package com.company.authorizationservice.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
    int statusCode,
    String message,
    String errorDetails,
    LocalDateTime timestamp
) {
    public ErrorResponseDTO(int statusCode, String message, String errorDetails) {
        this(statusCode, message, errorDetails, LocalDateTime.now());
    }
}
```
* **Purpose**: Guarantees that every error returned by the application follows a predictable JSON structure (`statusCode`, `message`, `errorDetails`, `timestamp`).

---

## 2. Persistence Layer (`entity` & `repository`)

### 2.1 `MyUser.java` (JPA Entity)
```java
package com.company.authorizationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "myuser")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyUser {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "token")
    private String token;

    public MyUser(String userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }
}
```
* **Key Annotations**:
  * `@Entity`: Marks this class as a JPA entity managed by Hibernate.
  * `@Table(name = "myuser")`: Explicitly names the SQL table in H2.
  * `@Column(name = "user_id")`: Overrides default naming so `userId` maps to SQL column `user_id`.
  * `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`: Lombok annotations that autogenerate getters, setters, and constructors.

---

### 2.2 `UserRepository.java` (Spring Data JPA Interface)
```java
package com.company.authorizationservice.repository;

import com.company.authorizationservice.entity.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<MyUser, String> {
    Optional<MyUser> findByUsername(String username);
}
```
* **`Optional<MyUser>`**: Returning `Optional` prevents `NullPointerException` if a user is missing from the database.
* **Spring Data Derived Query**: Spring Data automatically parses the method name `findByUsername` into:
  `SELECT * FROM myuser WHERE username = ?`

---

## 3. Cryptography & Security Layer (`security`)

### 3.1 `JwtUtil.java` (JJWT 0.12.x Engine)
```java
package com.company.authorizationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:defaultSuperSecretKeyForReturnOrderManagementSystem2026!}")
    private String secret;

    @Value("${jwt.expiration.ms:1800000}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
```
* **Key Innovations in JJWT 0.12.x**:
  1. `Keys.hmacShaKeyFor(...)`: Guarantees a minimum 256-bit key length required for HMAC-SHA256.
  2. `Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token)`: Type-safe parser replacing deprecated `.setSigningKey(String)`.
  3. `.claims()`, `.subject()`, `.issuedAt()`, `.expiration()`: Modern fluent builder methods without legacy `set` prefixes.

---

### 3.2 `JwtRequestFilter.java` (HTTP Security Interceptor)
```java
package com.company.authorizationservice.security;

import com.company.authorizationservice.service.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final MyUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("Unable to extract username from JWT token: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        chain.doFilter(request, response);
    }
}
```
* **Role**: Runs once for every incoming HTTP request. If `Authorization: Bearer <TOKEN>` header is present, it validates the token signature and registers the user inside `SecurityContextHolder`.

---

### 3.3 `SecurityConfig.java` (Spring Security 6 Architecture)
```java
package com.company.authorizationservice.security;

import com.company.authorizationservice.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MyUserDetailsService userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/authenticate", "/validate",
                                "/h2-console/**", "/actuator/**",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @SuppressWarnings("deprecation")
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
```
* **Key Architecture**:
  1. `SecurityFilterChain`: Replaces `WebSecurityConfigurerAdapter`.
  2. `SessionCreationPolicy.STATELESS`: Disables server-side HTTP sessions for REST microservices.
  3. `.requestMatchers(...).permitAll()`: Publicly exposes `/login`, `/validate`, `/h2-console`, `/actuator`, and Swagger UI (`/swagger-ui.html`).

---

## 4. User Details Service (`service`)

### 4.1 `MyUserDetailsService.java`
```java
package com.company.authorizationservice.service;

import com.company.authorizationservice.entity.MyUser;
import com.company.authorizationservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MyUser myUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new User(
                myUser.getUsername(),
                myUser.getPassword(),
                Collections.emptyList()
        );
    }
}
```
* **Adapter Pattern**: Bridges our database entity `MyUser` to Spring Security's `UserDetails` object.

---

## 5. Global Exception Handler (`exception`)

### 5.1 `GlobalExceptionHandler.java`
```java
package com.company.authorizationservice.exception;

import com.company.authorizationservice.dto.ErrorResponseDTO;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException ex) {
        log.error("Authentication failed: Invalid credentials");
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid Username or Password",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(UsernameNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "User Not Found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponseDTO> handleExpiredJwt(ExpiredJwtException ex) {
        log.error("JWT token expired: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "JWT Token Expired",
                "The submitted JWT token has expired. Please login again."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidJwt(JwtException ex) {
        log.error("Invalid JWT token: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid JWT Token",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce("", (a, b) -> a + "; " + b);

        log.error("Validation failed: {}", details);
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex) {
        log.error("Unhandled Exception: ", ex);
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```
* **`@RestControllerAdvice`**: Intercepts exceptions thrown anywhere in the controllers or filters and returns formatted `ErrorResponseDTO` responses.
