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
        // Fetch user from H2 database or throw Spring Security Exception if missing
        MyUser myUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Adapt our database entity (MyUser) into Spring Security's UserDetails object
        return new User(
                myUser.getUsername(),
                myUser.getPassword(),
                Collections.emptyList() // Empty list of granted authorities/roles for now
        );
    }
}