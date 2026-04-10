package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.CreateProjectRequest;
import com.example.sprintsight.dtos.requests.UpdateProjectRequest;
import com.example.sprintsight.dtos.responses.ProjectResponse;
import com.example.sprintsight.entities.Project;
import com.example.sprintsight.entities.User;
import com.example.sprintsight.mappers.ProjectMapper;
import com.example.sprintsight.repositories.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final UserService userService;
    private final ProjectMapper projectMapper;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID id) {
        return projectMapper.toProjectResponse(findProject(id));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getOwnedProjects(UUID userId) {
        return projectRepository.findByCreatedBy_Id(userId)
                .stream()
                .map(projectMapper::toProjectResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getMemberProjects(UUID userId) {
        return projectRepository.findAllProjectsByMemberId(userId)
                .stream()
                .map(projectMapper::toProjectResponse)
                .toList();
    }

    @Transactional
    public ProjectResponse addProject(CreateProjectRequest request, UUID principalId) {
        User user = userService.findUser(principalId);

        Project project = projectMapper.toEntity(request);
        project.setCreatedBy(user);

        return saveProject(project, "Create project");
    }

    @Transactional
    public ProjectResponse updateProject(
            UpdateProjectRequest request,
            UUID projectId,
            UUID principalId,
            boolean isPut
    ) {
        Project project = findProject(projectId);

        verifyOwnership(project, principalId);

        if (isPut) {
            projectMapper.updateProjectFromPut(request, project);
        }
        else {
            projectMapper.updateProjectFromPatch(request, project);
        }

        return saveProject(project, "Updated project");
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID principalId) {
        Project project = findProject(projectId);

        verifyOwnership(project, principalId);

        projectRepository.delete(project);

        log.info("Deleted project: {}", projectId);
    }

    protected Project findProject(UUID id) {
        return projectRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Project not found"));
    }

    private void verifyOwnership(Project project, UUID principalId) {
        if (!project.getCreatedBy().getId().equals(principalId)) {
            throw new AccessDeniedException("You do not have permission to modify this project");
        }
    }

    private ProjectResponse saveProject(Project project, String logMessage) {
        Project savedProject = projectRepository.save(project);

        log.info("{}: {}", logMessage, savedProject.getId());

        return projectMapper.toProjectResponse(savedProject);
    }
}
