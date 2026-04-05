package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.InvitationStatus;
import com.example.sprintsight.entities.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, UUID> {
    List<ProjectInvitation> findByReceiver_IdAndStatus(UUID receiverId, InvitationStatus status);
    List<ProjectInvitation> findByProject_IdAndStatus(UUID projectId, InvitationStatus status);
    boolean existsByProject_IdAndReceiver_IdAndStatus(UUID projectId, UUID receiverId, InvitationStatus status);
}
