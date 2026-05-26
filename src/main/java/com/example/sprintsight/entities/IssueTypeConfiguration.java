package com.example.sprintsight.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(
        name = "issue_type_configs",
        indexes = {
                @Index(name = "issue_type_project_idx", columnList = "project_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "issue_type_project_name_key",
                        columnNames = {"project_id", "name"}
                )
        }
)
public class IssueTypeConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private boolean isDefault = false;
}
