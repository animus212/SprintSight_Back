package com.example.SprintSight.DTOs;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private UUID id;


    @Column(length = 50, nullable = false)
    private String username;


    @Size(min = 8, max = 255)
    private String password;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 100)
    private String fullName;

    @Size(max = 500)
    private String bio;
}
