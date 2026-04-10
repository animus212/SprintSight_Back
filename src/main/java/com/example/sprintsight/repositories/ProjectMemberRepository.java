package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.ProjectMember;
import com.example.sprintsight.entities.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    List<ProjectMember> findById_UserId(UUID userId);
    List<ProjectMember> findById_ProjectId(UUID projectId);
    boolean existsById_UserId(UUID userId);
    boolean existsById_UserIdAndId_ProjectId(UUID userId, UUID projectId);
}
