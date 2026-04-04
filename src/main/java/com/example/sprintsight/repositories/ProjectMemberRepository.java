package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.ProjectMember;
import com.example.sprintsight.entities.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    Optional<List<ProjectMember>> findByUserId();

    Optional<List<ProjectMember>> findByProjectId(UUID projectId);

    boolean existsByUserId(UUID userId);

}
