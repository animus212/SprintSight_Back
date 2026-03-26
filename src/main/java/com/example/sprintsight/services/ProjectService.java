package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.CreateProjectRequest;
import com.example.sprintsight.dtos.requests.UpdateProjectRequest;
import com.example.sprintsight.dtos.responses.ProjectResponse;
import com.example.sprintsight.entities.Project;
import com.example.sprintsight.entities.User;
import com.example.sprintsight.exceptions.EntityNotFoundException;
import com.example.sprintsight.mappers.ProjectMapper;
import com.example.sprintsight.repositories.ProjectRepository;
import com.example.sprintsight.repositories.UserRepository;
import com.example.sprintsight.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    public ProjectResponse getProject(UUID id) {
        return projectMapper.toProjectResponse(findProject(id));
    }

    public List<ProjectResponse> getProjects() {
        return projectRepository.findAll()
                .stream()
                .map(projectMapper::toProjectResponse)
                .toList();
    }

    @Transactional
    public ProjectResponse addProject(CreateProjectRequest request) {
        UserPrincipal principal = (UserPrincipal) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()
        ).getPrincipal();

        assert principal != null;
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Project project = projectMapper.toEntity(request);
        project.setCreatedBy(user);

        return saveProject(project, "Create project");
    }

    @Transactional
    public ProjectResponse updateProject(UpdateProjectRequest request, UUID id, boolean isPut) {
        Project project = findProject(id);

        if (isPut) {
            projectMapper.updateProjectFromPut(request, project);
        }
        else {
            projectMapper.updateProjectFromPatch(request, project);
        }

        return saveProject(project, "Updated project");
    }

    @Transactional
    public void deleteProject(UUID id) {
        Project project = findProject(id);

        projectRepository.delete(project);

        log.info("Deleted project: {}", id);
    }

    private Project findProject(UUID id) {
        return projectRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Project not found"));
    }

    private ProjectResponse saveProject(Project project, String logMessage) {
        Project savedProject = projectRepository.save(project);

        log.info("{}: {}", logMessage, savedProject.getId());

        return projectMapper.toProjectResponse(savedProject);
    }
}
