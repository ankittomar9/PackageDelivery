package com.company.authorizationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="myuser")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class MyUser {

    @Id
    @Column(name="user_id")
    private String userId;

    @Column(name = "username")
    private String username;

    @Column(name="password")
    private String password;

    @Column(name="token")
    private String token;

    // Optional: Custom 3-arg constructor if you want to create a user without a token initially
    public MyUser(String userId,  String password, String token) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.token = token;
    }
}
