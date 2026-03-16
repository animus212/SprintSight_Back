package com.example.SprintSight.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
