package com.example.sprintsight.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password"})
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "users_username_key", columnNames = "username"),
                @UniqueConstraint(name = "users_email_key", columnNames = "email")
        },
        indexes = {
                @Index(name = "users_username_idx", columnList = "username"),
                @Index(name = "users_email_idx", columnList = "email")
        }
)
public class User {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 50, nullable = false)
    private String username;

    @Column(length = 128, nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(length = 100)
    private String fullName;

    @Column(length = 500)
    private String bio;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole = UserRole.USER;

    @Column(nullable = false)
    private boolean enabled = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
