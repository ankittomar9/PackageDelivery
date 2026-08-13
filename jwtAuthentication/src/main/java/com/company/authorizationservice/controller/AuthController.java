package com.company.authorizationservice.controller;

import com.company.authorizationservice.dto.AuthRequestDTO;
import com.company.authorizationservice.dto.AuthResponseDTO;
import com.company.authorizationservice.security.JwtUtil;
import com.company.authorizationservice.service.MyUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication Controller", description = "Endpoints for user login and JWT token validation")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MyUserDetailsService userDetailsService;

    @Operation(summary = "User Login", description = "Authenticates user credentials and issues a signed JWT token")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated and token generated")
    @ApiResponse(responseCode = "401", description = "Invalid credentials provided")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO authRequest) {
        log.info("Received authentication request for user: {}", authRequest.username());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
            );
        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user: {}", authRequest.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(null, false));
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.username());
        final String jwt = jwtUtil.generateToken(userDetails);

        log.info("Successfully generated JWT token for user: {}", authRequest.username());
        return ResponseEntity.ok(new AuthResponseDTO(jwt, true));
    }

    @Operation(summary = "Validate Token", description = "Verifies the validity and expiration of a JWT token")
    @ApiResponse(responseCode = "200", description = "Token is valid")
    @ApiResponse(responseCode = "401", description = "Token is invalid or expired")
    @GetMapping("/validate")
    public ResponseEntity<AuthResponseDTO> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Received token validation request");

        String token = authHeader;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        boolean isValid = jwtUtil.validateToken(token);

        if (isValid) {
            String username = jwtUtil.extractUsername(token);
            log.info("Token validated successfully for user: {}", username);
            return ResponseEntity.ok(new AuthResponseDTO(token, null, "Bearer", 1800000L, username, true));
        } else {
            log.warn("Invalid or expired token submitted for validation");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(null, false));
        }
    }
}