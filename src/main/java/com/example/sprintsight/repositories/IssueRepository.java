package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {
    @EntityGraph(attributePaths = {"type", "priority", "status", "project", "createdBy", "assignedTo", "components"})
    Optional<Issue> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"type", "priority", "status", "assignedTo"})
    List<Issue> findByProject_Id(UUID projectId);

    @EntityGraph(attributePaths = {"type", "priority", "status", "assignedTo"})
    Page<Issue> findByProject_Id(UUID projectId, Pageable pageable);

    @EntityGraph(attributePaths = {"type", "priority", "status", "assignedTo"})
    List<Issue> findByProject_IdAndAssignedTo_Id(UUID projectId, UUID userId);

    @EntityGraph(attributePaths = {"type", "priority", "status", "project", "assignedTo"})
    List<Issue> findByAssignedTo_Id(UUID userId);

    @EntityGraph(attributePaths = {"type", "priority", "status", "project", "assignedTo"})
    Page<Issue> findByAssignedTo_Id(UUID userId, Pageable pageable);

    boolean existsByIdAndProject_Id(UUID issueId, UUID projectId);
    boolean existsByType_Id(UUID typeId);
    boolean existsByPriority_Id(UUID priorityId);
    boolean existsByStatus_Id(UUID statusId);
}
