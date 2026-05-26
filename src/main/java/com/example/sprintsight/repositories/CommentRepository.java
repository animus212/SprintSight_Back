package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @EntityGraph(attributePaths = {"author"})
    List<Comment> findByIssue_IdOrderByCreatedAtAsc(UUID issueId);

    @EntityGraph(attributePaths = {"author"})
    Page<Comment> findByIssue_Id(UUID issueId, Pageable pageable);
}
