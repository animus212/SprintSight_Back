package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.UpdateProjectMemberRequest;
import com.example.sprintsight.dtos.responses.ProjectMemberResponse;
import com.example.sprintsight.entities.*;
import com.example.sprintsight.mappers.ProjectMemberMapper;
import com.example.sprintsight.repositories.ProjectMemberRepository;
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
    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectMemberRepository projectMemberRepository;

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
    protected void addProjectMember(UUID userId, ProjectRole projectRole, UUID projectId) {
        User user = userService.findUser(userId);
        Project project = projectService.findProject(projectId);

        if (projectMemberRepository.existsById_UserIdAndId_ProjectId(userId, projectId)) {
            throw new IllegalStateException("User is already a member of this project");
        }

        ProjectMemberId id = new ProjectMemberId(projectId, userId);
        ProjectMember projectMember = new ProjectMember();
        projectMember.setId(id);
        projectMember.setUser(user);
        projectMember.setProject(project);
        projectMember.setProjectRole(projectRole);

        saveProjectMember(projectMember, "Added project member");
    }

    @Transactional
    public ProjectMemberResponse updateProjectMember(
            UpdateProjectMemberRequest request,
            UUID userId,
            UUID projectId
    ) {
        ProjectMember projectMember = findProjectMember(userId, projectId);

        projectMemberMapper.updateProjectMemberFromPatch(request, projectMember);

        return saveProjectMember(projectMember, "Updated project member");
    }

    @Transactional
    public void deleteProjectMember(UUID userId, UUID projectId) {
        ProjectMember projectMember = findProjectMember(userId, projectId);

        projectMemberRepository.delete(projectMember);

        log.info("Deleted project member: {}, {}", userId, projectId);
    }

    private ProjectMember findProjectMember(UUID userId, UUID projectId) {
        return projectMemberRepository.findById(new ProjectMemberId(projectId, userId))
                .orElseThrow(() -> new EntityNotFoundException("Project member not found"));
    }

    private ProjectMemberResponse saveProjectMember(ProjectMember projectMember, String logMessage) {
        ProjectMember saved = projectMemberRepository.save(projectMember);

        log.info("{}: {}", logMessage, saved.getId());

        return projectMemberMapper.toProjectMemberResponse(saved);
    }
}
