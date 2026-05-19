package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {
    List<Issue> findByProject_Id(UUID projectId);
    List<Issue> findByProject_IdAndAssignedTo_Id(UUID projectId, UUID userId);
    List<Issue> findByAssignedTo_Id(UUID userId);
    boolean existsByIdAndProject_Id(UUID issueId, UUID projectId);
    boolean existsByType_Id(UUID typeId);
    boolean existsByPriority_Id(UUID priorityId);
    boolean existsByStatus_Id(UUID statusId);
}
