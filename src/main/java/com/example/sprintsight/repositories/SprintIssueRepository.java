package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.SprintIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintIssueRepository extends JpaRepository<SprintIssue, UUID> {
    List<SprintIssue> findBySprint_Id(UUID sprintId);
    List<SprintIssue> findBySprint_IdAndRemovedAtIsNull(UUID sprintId);
    Optional<SprintIssue> findBySprint_IdAndIssue_IdAndRemovedAtIsNull(UUID sprintId, UUID issueId);
    boolean existsBySprint_IdAndIssue_IdAndRemovedAtIsNull(UUID sprintId, UUID issueId);
}
