package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.ComponentRequest;
import com.example.sprintsight.dtos.responses.ComponentResponse;
import com.example.sprintsight.dtos.responses.IssueResponse;
import com.example.sprintsight.entities.*;
import com.example.sprintsight.exceptions.BusinessRuleViolationException;
import com.example.sprintsight.exceptions.ResourceConflictException;
import com.example.sprintsight.mappers.ComponentMapper;
import com.example.sprintsight.mappers.IssueMapper;
import com.example.sprintsight.repositories.ComponentRepository;
import com.example.sprintsight.repositories.IssueRepository;
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
    private final IssueMapper issueMapper;
    private final IssueRepository issueRepository;

    @Transactional(readOnly = true)
    public List<ComponentResponse> getComponents(UUID projectId, UUID principalId) {
        authorizationService.getMemberOrThrow(principalId, projectId);

        List<ComponentResponse> componentResponses = componentRepository.findByProject_Id(projectId)
                .stream()
                .map(componentMapper::toComponentResponse)
                .toList();

        for(ComponentResponse component : componentResponses){
            List<IssueResponse> issueResponses =
                    issueMapper.toIssueResponses(
                            issueRepository.findByComponents_Id(component.id())
                    );
            component.issues().addAll(issueResponses);
        }
        return componentResponses;
    }

    @Transactional
    public ComponentResponse createComponent(ComponentRequest request, UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        if (componentRepository.existsByNameAndProject_Id(request.name(), projectId)) {
            throw new ResourceConflictException(
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

        if (componentRepository.isReferencedByAnyIssue(componentId)) {
            throw new BusinessRuleViolationException(
                    "Cannot delete this component — it is assigned to one or more issues. " +
                            "Remove it from those issues first.");
        }

        componentRepository.delete(component);

        log.info("Deleted component {}", componentId);
    }
}
