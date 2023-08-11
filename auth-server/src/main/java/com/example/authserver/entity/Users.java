package com.example.authserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    @NotBlank(message = "Name cannot be null")
    private String name;

    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 8, message = "Password must be 8 characters long")
    private String password;

    @Column(nullable = false)
    private boolean isEmailVerified;

    public Users(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.isEmailVerified = false;
    }
}
