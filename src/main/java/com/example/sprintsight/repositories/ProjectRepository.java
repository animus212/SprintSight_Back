package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByCreatedBy_Id(UUID userId);

    @Query("""
        SELECT p FROM Project p
        JOIN ProjectMember pm ON pm.project = p
        WHERE pm.id.userId = :userId
    """)
    List<Project> findAllProjectsByMemberId(@Param("userId") UUID userId);
}
