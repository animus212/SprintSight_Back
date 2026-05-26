package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.IssueEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IssueEventRepository extends JpaRepository<IssueEvent, UUID> {
    @EntityGraph(attributePaths = {"changedBy"})
    List<IssueEvent> findByIssue_IdOrderByChangedAtAsc(UUID issueId);

    @EntityGraph(attributePaths = {"changedBy"})
    Page<IssueEvent> findByIssue_Id(UUID issueId, Pageable pageable);

    @EntityGraph(attributePaths = {"issue"})
    Page<IssueEvent> findByChangedBy_IdOrderByChangedAtDesc(UUID userId, Pageable pageable);
}
