package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.ProjectRequest;
import com.example.sprintsight.dtos.responses.ImageUrlResponse;
import com.example.sprintsight.dtos.responses.ProjectResponse;
import com.example.sprintsight.entities.Project;
import com.example.sprintsight.entities.ProjectRole;
import com.example.sprintsight.entities.User;
import com.example.sprintsight.mappers.ProjectMapper;
import com.example.sprintsight.repositories.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final UserService userService;
    private final IssueConfigurationService issueConfigurationService;
    private final ProjectMapper projectMapper;
    private final ProjectRepository projectRepository;
    private final ProjectAuthorizationService authorizationService;
    private final CloudinaryImageService cloudinaryImageService;
    private final ProjectMemberService projectMemberService;

    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID id, UUID principalId) {
        authorizationService.getMemberOrThrow(principalId, id);

        Project project = projectRepository.findWithCreatedBy(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        return projectMapper.toProjectResponse(project);
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
    public ProjectResponse addProject(ProjectRequest request, UUID principalId) {
        User user = userService.findUser(principalId);

        Project project = projectMapper.toEntity(request);
        project.setCreatedBy(user);

        Project saved = projectRepository.save(project);

        issueConfigurationService.seedDefaults(saved);

        log.info("Created project {}", saved.getId());

        projectMemberService.addProjectMember(user.getId(), ProjectRole.PRODUCT_OWNER, saved.getId());

        return projectMapper.toProjectResponse(saved);
    }

    @Transactional
    public ProjectResponse updateProject(ProjectRequest request, UUID projectId, UUID principalId) {
        Project project = findProject(projectId);

        verifyOwnership(project, principalId);

        projectMapper.updateProjectFromRequest(request, project);

        Project saved = projectRepository.save(project);

        log.info("Updated project {}", saved.getId());

        return projectMapper.toProjectResponse(saved);
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID principalId) {
        Project project = findProject(projectId);

        verifyOwnership(project, principalId);

        cloudinaryImageService.deleteByUrl(project.getImageUrl());

        projectRepository.delete(project);

        log.info("Deleted project: {}", projectId);
    }

    @Transactional
    public ImageUrlResponse updateProjectImage(UUID projectId, MultipartFile file, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        Project project = findProject(projectId);
        String oldUrl = project.getImageUrl();

        String newUrl = cloudinaryImageService.uploadProjectImage(file).url();
        project.setImageUrl(newUrl);
        projectRepository.save(project);

        cloudinaryImageService.deleteByUrl(oldUrl);

        log.info("Updated image for project {} by {}", projectId, principalId);
        return new ImageUrlResponse(newUrl);
    }

    @Transactional
    public void deleteProjectImage(UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        Project project = findProject(projectId);
        String oldUrl = project.getImageUrl();

        if (oldUrl == null || oldUrl.isBlank()) {
            return;
        }

        project.setImageUrl(null);
        projectRepository.save(project);

        cloudinaryImageService.deleteByUrl(oldUrl);
        log.info("Removed image for project {} by {}", projectId, principalId);
    }

    public Project findProject(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
    }

    private void verifyOwnership(Project project, UUID principalId) {
        if (!project.getCreatedBy().getId().equals(principalId)) {
            throw new AccessDeniedException("You do not have permission to modify this project");
        }
    }
}
