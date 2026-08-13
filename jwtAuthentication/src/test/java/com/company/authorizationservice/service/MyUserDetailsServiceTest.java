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
        // Given: Mock UserRepository behavior
        MyUser mockUser = new MyUser("1", "admin", "admin", null);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mockUser));

        // When: Call service method
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
        // Given: Mock UserRepository returning empty Optional
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When & Then: Expect exception
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("unknown");
        });
        verify(userRepository, times(1)).findByUsername("unknown");
    }
}