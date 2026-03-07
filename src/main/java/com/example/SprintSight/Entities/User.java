package com.example.SprintSight.Entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_username", columnList = "username"),
                @Index(name = "idx_email", columnList = "email")
        }
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(length = 50, nullable = false)
    private String username;

    @NotBlank
    @Size(min = 8, max = 255)
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String email;

    @Size(max = 100)
    @Column(length = 100)
    private String fullName;

    @Size(max = 500)
    @Column(length = 500)
    private String bio;
}
