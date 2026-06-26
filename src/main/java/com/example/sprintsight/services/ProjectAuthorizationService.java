package com.example.sprintsight.services;

import com.example.sprintsight.entities.ProjectMember;
import com.example.sprintsight.entities.ProjectMemberId;
import com.example.sprintsight.entities.ProjectRole;
import com.example.sprintsight.repositories.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectAuthorizationService {
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMember getMemberOrThrow(UUID userId, UUID projectId) {
        return projectMemberRepository.findById(new ProjectMemberId(userId, projectId))
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project"));
    }

    public void requireAnyRole(UUID userId, UUID projectId, ProjectRole... roles) {
        ProjectMember member = getMemberOrThrow(userId, projectId);

        if (Arrays.stream(roles).noneMatch(r -> r == member.getProjectRole())) {
            throw new AccessDeniedException("Insufficient permissions for this action");
        }
    }

    public boolean hasAnyRole(UUID userId, UUID projectId, ProjectRole... roles) {
        return projectMemberRepository.findById(new ProjectMemberId(userId, projectId))
                .map(m -> Arrays.stream(roles).anyMatch(r -> r == m.getProjectRole()))
                .orElse(false);
    }

    public ProjectRole getRoleOrThrow(UUID userId, UUID projectId) {
        return getMemberOrThrow(userId, projectId).getProjectRole();
    }
}
