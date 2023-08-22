package com.example.authserver.entity;

import com.example.authserver.validation.annotation.PasswordComplexity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "api.user.entity.name")
    @Column(unique = true)
    private String name;

    @Email(message = "api.user.entity.mail")
    @Column(unique = true)
    private String email;

    @PasswordComplexity
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
