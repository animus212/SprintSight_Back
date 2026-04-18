package com.example.sprintsight.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "issues", indexes = {
        @Index(name = "issues_project_idx",  columnList = "project_id"),
        @Index(name = "issues_assignee_idx", columnList = "assigned_to"),
        @Index(name = "issues_sprint_idx",   columnList = "sprint_id")
})
public class Issue {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IssueType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IssuePriority priority = IssuePriority.LOW;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IssueStatus status = IssueStatus.TODO;

    @Column
    private Integer storyPoints;

    @Column(length = 50)
    private String fixVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    @JoinColumn(name = "assigned_to")
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User assignedTo;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "issue_components",
            joinColumns = @JoinColumn(name = "issue_id"),
            inverseJoinColumns = @JoinColumn(name = "component_id")
    )
    private Set<Component> components = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
