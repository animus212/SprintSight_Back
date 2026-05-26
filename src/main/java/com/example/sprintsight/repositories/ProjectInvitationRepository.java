package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.InvitationStatus;
import com.example.sprintsight.entities.ProjectInvitation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, UUID> {
    @EntityGraph(attributePaths = {"project", "sender", "receiver"})
    List<ProjectInvitation> findByReceiver_IdAndStatus(UUID receiverId, InvitationStatus status);

    @EntityGraph(attributePaths = {"project", "sender", "receiver"})
    Page<ProjectInvitation> findByReceiver_IdAndStatus(UUID receiverId, InvitationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"project", "sender", "receiver"})
    List<ProjectInvitation> findByProject_IdAndStatus(UUID projectId, InvitationStatus status);

    @EntityGraph(attributePaths = {"project", "sender", "receiver"})
    Page<ProjectInvitation> findByProject_Id(UUID projectId, Pageable pageable);

    boolean existsByProject_IdAndReceiver_IdAndStatus(UUID projectId, UUID receiverId, InvitationStatus status);
}
