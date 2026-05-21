package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.UpdateProjectMemberRequest;
import com.example.sprintsight.dtos.responses.ProjectMemberResponse;
import com.example.sprintsight.entities.*;
import com.example.sprintsight.mappers.ProjectMemberMapper;
import com.example.sprintsight.repositories.ProjectMemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectMemberRepository projectMemberRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getAllProjectMembers(UUID projectId) {
        return projectMemberRepository.findById_ProjectId(projectId)
                .stream()
                .map(projectMemberMapper::toProjectMemberResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getAllMemberProjects(UUID userId) {
        return projectMemberRepository.findById_UserId(userId)
                .stream()
                .map(projectMemberMapper::toProjectMemberResponse)
                .toList();
    }

    @Transactional
    public void addProjectMember(UUID userId, ProjectRole projectRole, UUID projectId) {
        if (projectMemberRepository.existsById_UserIdAndId_ProjectId(userId, projectId)) {
            throw new IllegalStateException("User is already a member of this project");
        }

        User userRef = entityManager.getReference(User.class, userId);
        Project projectRef = entityManager.getReference(Project.class, projectId);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setId(new ProjectMemberId(projectId, userId));
        projectMember.setUser(userRef);
        projectMember.setProject(projectRef);
        projectMember.setProjectRole(projectRole);

        projectMemberRepository.save(projectMember);

        log.info("Added project member: user={}, project={}, role={}", userId, projectId, projectRole);
    }

    @Transactional
    public ProjectMemberResponse updateProjectMember(
            UpdateProjectMemberRequest request,
            UUID userId,
            UUID projectId
    ) {
        ProjectMember projectMember = findProjectMember(userId, projectId);

        projectMemberMapper.updateProjectMemberFromRequest(request, projectMember);

        ProjectMember saved = projectMemberRepository.save(projectMember);

        log.info("Updated project member: user={}, project={}", userId, projectId);

        return projectMemberMapper.toProjectMemberResponse(saved);
    }

    @Transactional
    public void deleteProjectMember(UUID userId, UUID projectId) {
        ProjectMember projectMember = findProjectMember(userId, projectId);

        projectMemberRepository.delete(projectMember);

        log.info("Deleted project member: user={}, project={}", userId, projectId);
    }

    private ProjectMember findProjectMember(UUID userId, UUID projectId) {
        return projectMemberRepository.findById(new ProjectMemberId(projectId, userId))
                .orElseThrow(() -> new EntityNotFoundException("Project member not found"));
    }
}
