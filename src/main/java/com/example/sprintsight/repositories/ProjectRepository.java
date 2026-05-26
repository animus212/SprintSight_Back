package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.Project;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @Query("SELECT p FROM Project p JOIN FETCH p.createdBy WHERE p.id = :id")
    Optional<Project> findWithCreatedBy(@Param("id") UUID id);

    List<Project> findByCreatedBy_Id(UUID userId);
    Page<Project> findByCreatedBy_Id(UUID userId, Pageable pageable);

    @Query("""
        SELECT DISTINCT p FROM Project p
        JOIN FETCH p.createdBy
        JOIN ProjectMember pm ON pm.project = p
        WHERE pm.id.userId = :userId
    """)
    List<Project> findAllProjectsByMemberId(@Param("userId") UUID userId);

    @Query(value = """
        SELECT DISTINCT p FROM Project p
        JOIN FETCH p.createdBy
        JOIN ProjectMember pm ON pm.project = p
        WHERE pm.id.userId = :userId
    """,
            countQuery = """
        SELECT COUNT(DISTINCT p) FROM Project p
        JOIN ProjectMember pm ON pm.project = p
        WHERE pm.id.userId = :userId
    """)
    Page<Project> findAllProjectsByMemberId(@Param("userId") UUID userId, Pageable pageable);
}
