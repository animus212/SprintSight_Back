package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.ProjectMember;
import com.example.sprintsight.entities.ProjectMemberId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    @EntityGraph(attributePaths = {"user", "project"})
    List<ProjectMember> findById_UserId(UUID userId);

    @EntityGraph(attributePaths = {"user", "project"})
    List<ProjectMember> findById_ProjectId(UUID projectId);

    @EntityGraph(attributePaths = {"user", "project"})
    Page<ProjectMember> findById_UserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "project"})
    Page<ProjectMember> findById_ProjectId(UUID projectId, Pageable pageable);

    boolean existsById_UserIdAndId_ProjectId(UUID userId, UUID projectId);
}
