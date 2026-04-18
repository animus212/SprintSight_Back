package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.ComponentRequest;
import com.example.sprintsight.dtos.responses.ComponentResponse;
import com.example.sprintsight.entities.*;
import com.example.sprintsight.mappers.ComponentMapper;
import com.example.sprintsight.repositories.ComponentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentService {
    private final ComponentRepository componentRepository;
    private final ProjectService projectService;
    private final ProjectAuthorizationService authorizationService;
    private final ComponentMapper componentMapper;

    @Transactional(readOnly = true)
    public List<ComponentResponse> getComponents(UUID projectId, UUID principalId) {
        authorizationService.getMemberOrThrow(principalId, projectId);
        return componentRepository.findByProject_Id(projectId)
                .stream()
                .map(componentMapper::toComponentResponse)
                .toList();
    }

    @Transactional
    public ComponentResponse createComponent(ComponentRequest request, UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        if (componentRepository.existsByNameAndProject_Id(request.name(), projectId)) {
            throw new IllegalStateException(
                    "A component named '" + request.name() + "' already exists in this project");
        }

        Project project = projectService.findProject(projectId);
        Component component = componentMapper.toEntity(request);
        component.setProject(project);

        Component saved = componentRepository.save(component);
        log.info("Created component {} in project {}", saved.getId(), projectId);
        return componentMapper.toComponentResponse(saved);
    }

    @Transactional
    public void deleteComponent(UUID componentId, UUID principalId) {
        Component component = componentRepository.findById(componentId)
                .orElseThrow(() -> new EntityNotFoundException("Component not found"));

        authorizationService.requireAnyRole(principalId, component.getProject().getId(),
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        componentRepository.delete(component);
        log.info("Deleted component {}", componentId);
    }
}
