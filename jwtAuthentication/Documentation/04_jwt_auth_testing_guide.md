# Software Testing & Unit/Integration Testing Guide
## Microservice: `jwtAuthentication`

---

## 1. Testing Philosophy & Terminology

Software testing ensures that application logic behaves predictably, fails gracefully under bad input, and prevents regressions when refactoring.

### Core Testing Pillars:
1. **Unit Testing**:
   * Tests a single class in complete isolation.
   * **No Spring Context** or Database is loaded.
   * Dependencies are mocked using **Mockito** (`@Mock`, `@InjectMocks`).
   * Execution time: Extremely fast (milliseconds).
2. **Web Layer Integration Testing (`@WebMvcTest`)**:
   * Tests REST Endpoints (`@RestController`) and HTTP Status Codes.
   * Uses **`MockMvc`** to perform simulated HTTP requests (`post()`, `get()`) without starting a full HTTP web server.
   * Uses **`@MockitoBean`** to mock spring beans injected into controllers.

---

## 2. Unit Test Specifications & Code

### 2.1 Testing `JwtUtil` (`JwtUtilTest.java`)

```java
package com.company.authorizationservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // ReflectionTestUtils sets private @Value fields without needing Spring container
        ReflectionTestUtils.setField(jwtUtil, "secret", "defaultSuperSecretKeyForReturnOrderManagementSystem2026!");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 1800000L);

        userDetails = new User("admin", "admin", Collections.emptyList());
    }

    @Test
    @DisplayName("Should generate a non-null JWT token for valid UserDetails")
    void testGenerateToken() {
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should correctly extract username from valid token")
    void testExtractUsername() {
        String token = jwtUtil.generateToken(userDetails);
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals("admin", extractedUsername);
    }

    @Test
    @DisplayName("Should return true for valid token")
    void testValidateToken_Success() {
        String token = jwtUtil.generateToken(userDetails);
        Boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should return false for malformed token string")
    void testValidateToken_InvalidToken() {
        Boolean isValid = jwtUtil.validateToken("invalid.token.string");
        assertFalse(isValid);
    }
}
```

#### Key Assertions & Concepts:
* **`ReflectionTestUtils.setField(...)`**: Injects `@Value` properties into `jwtUtil` without booting the Spring ApplicationContext.
* **`assertNotNull()` / `assertEquals()` / `assertTrue()`**: Standard JUnit 5 assertion methods verifying expected output against actual output.

---

### 2.2 Testing Service Layer (`MyUserDetailsServiceTest.java`)

```java
package com.company.authorizationservice.service;

import com.company.authorizationservice.entity.MyUser;
import com.company.authorizationservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserDetailsService userDetailsService;

    @Test
    @DisplayName("Should load UserDetails when user exists in database")
    void testLoadUserByUsername_Success() {
        // Given: Mock database output
        MyUser mockUser = new MyUser("1", "admin", "admin", null);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mockUser));

        // When: Execute service method
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        // Then: Assert correct properties
        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertEquals("admin", userDetails.getPassword());
        verify(userRepository, times(1)).findByUsername("admin");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user does not exist")
    void testLoadUserByUsername_NotFound() {
        // Given: Empty Optional from repository
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When & Then: Expect exception
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("unknown");
        });
        verify(userRepository, times(1)).findByUsername("unknown");
    }
}
```

#### Mockito Concepts Explained:
* **`@Mock`**: Creates a fake, simulated instance of `UserRepository`.
* **`@InjectMocks`**: Instantiates `MyUserDetailsService` and injects the mocked `UserRepository` into it.
* **`when(...).thenReturn(...)`**: Configures stubbed behavior for mock methods.
* **`verify(..., times(1))`**: Asserts that `findByUsername(...)` was called exactly once during execution.

---

### 2.3 Web Layer Controller Test (`AuthControllerTest.java`)

```java
package com.company.authorizationservice.controller;

import com.company.authorizationservice.dto.AuthRequestDTO;
import com.company.authorizationservice.security.JwtUtil;
import com.company.authorizationservice.security.SecurityConfig;
import com.company.authorizationservice.service.MyUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private MyUserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /login - Should return 200 OK and JWT token on valid credentials")
    void testLogin_Success() throws Exception {
        AuthRequestDTO requestDTO = new AuthRequestDTO("admin", "admin");
        UserDetails mockUserDetails = new User("admin", "admin", Collections.emptyList());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(mockUserDetails);
        when(jwtUtil.generateToken(mockUserDetails)).thenReturn("mock.jwt.token");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @DisplayName("POST /login - Should return 401 Unauthorized on invalid password")
    void testLogin_InvalidCredentials() throws Exception {
        AuthRequestDTO requestDTO = new AuthRequestDTO("admin", "wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }
}
```

#### MockMvc Concepts Explained:
* **`MockMvc`**: Provides main entry point for Spring MVC testing without running HTTP servers.
* **`@MockitoBean`**: Spring Boot 3.4+ annotation registering a Mockito mock inside Spring's test `ApplicationContext`.
* **`andExpect(status().isOk())`**: Asserts HTTP 200 response status code.
* **`andExpect(jsonPath("$.jwtToken").value(...))`**: Uses JSONPath syntax to evaluate fields inside the returned JSON payload.
