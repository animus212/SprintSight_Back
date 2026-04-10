package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.SendInvitationRequest;
import com.example.sprintsight.dtos.responses.InvitationResponse;
import com.example.sprintsight.entities.InvitationStatus;
import com.example.sprintsight.entities.Project;
import com.example.sprintsight.entities.ProjectInvitation;
import com.example.sprintsight.entities.User;
import com.example.sprintsight.mappers.InvitationMapper;
import com.example.sprintsight.repositories.ProjectInvitationRepository;
import com.example.sprintsight.repositories.ProjectMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationService {
    private final UserService userService;
    private final ProjectService projectService;
    private final InvitationMapper invitationMapper;
    private final ProjectMemberService projectMemberService;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectInvitationRepository invitationRepository;

    @Transactional(readOnly = true)
    public List<InvitationResponse> getPendingInvitations(UUID userId) {
        return invitationRepository
                .findByReceiver_IdAndStatus(userId, InvitationStatus.PENDING)
                .stream()
                .map(invitationMapper::toInvitationResponse)
                .toList();
    }

    @Transactional
    public InvitationResponse sendInvitation(
            SendInvitationRequest request,
            UUID projectId,
            UUID senderId
    ) {
        User sender = userService.findUser(senderId);
        User receiver = userService.findUser(request.receiverId());
        Project project = projectService.findProject(projectId);

        if (projectMemberRepository.existsById_UserIdAndId_ProjectId(request.receiverId(), projectId)) {
            throw new IllegalStateException("User is already a member of this project");
        }

        if (invitationRepository.existsByProject_IdAndReceiver_IdAndStatus(
                projectId, request.receiverId(), InvitationStatus.PENDING)) {
            throw new IllegalStateException("A pending invitation already exists for this user");
        }

        ProjectInvitation invitation = invitationMapper.toEntity(request);
        invitation.setProject(project);
        invitation.setSender(sender);
        invitation.setReceiver(receiver);

        ProjectInvitation saved = invitationRepository.save(invitation);
        InvitationResponse response = invitationMapper.toInvitationResponse(saved);

        log.info("Invitation sent from {} to {} for project {}", senderId, request.receiverId(), projectId);

        return response;
    }

    @Transactional
    public InvitationResponse acceptInvitation(UUID invitationId, UUID principalId) {
        ProjectInvitation invitation = findPendingInvitation(invitationId, principalId);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(Instant.now());
        invitationRepository.save(invitation);

        projectMemberService.addProjectMember(
                principalId, invitation.getIntendedRole(), invitation.getProject().getId()
        );

        return invitationMapper.toInvitationResponse(invitation);
    }

    @Transactional
    public InvitationResponse rejectInvitation(UUID invitationId, UUID principalId) {
        ProjectInvitation invitation = findPendingInvitation(invitationId, principalId);

        invitation.setStatus(InvitationStatus.REJECTED);
        invitation.setRespondedAt(Instant.now());
        invitationRepository.save(invitation);

        return invitationMapper.toInvitationResponse(invitation);
    }

    private ProjectInvitation findPendingInvitation(UUID invitationId, UUID principalId) {
        ProjectInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));

        if (!invitation.getReceiver().getId().equals(principalId)) {
            throw new AccessDeniedException("You are not the recipient of this invitation");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation has already been responded to");
        }

        return invitation;
    }
}
