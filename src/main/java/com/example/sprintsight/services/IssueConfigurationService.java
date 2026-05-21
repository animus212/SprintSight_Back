package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.IssuePriorityRequest;
import com.example.sprintsight.dtos.requests.IssueStatusRequest;
import com.example.sprintsight.dtos.requests.IssueTypeRequest;
import com.example.sprintsight.dtos.responses.IssuePriorityConfigurationResponse;
import com.example.sprintsight.dtos.responses.IssueStatusConfigurationResponse;
import com.example.sprintsight.dtos.responses.IssueTypeConfigurationResponse;
import com.example.sprintsight.entities.*;
import com.example.sprintsight.exceptions.BusinessRuleViolationException;
import com.example.sprintsight.exceptions.ResourceConflictException;
import com.example.sprintsight.mappers.IssueConfigurationMapper;
import com.example.sprintsight.repositories.IssuePriorityConfigurationRepository;
import com.example.sprintsight.repositories.IssueRepository;
import com.example.sprintsight.repositories.IssueStatusConfigurationRepository;
import com.example.sprintsight.repositories.IssueTypeConfigurationRepository;
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
public class IssueConfigurationService {
    private final IssueTypeConfigurationRepository typeRepository;
    private final IssuePriorityConfigurationRepository priorityRepository;
    private final IssueStatusConfigurationRepository statusRepository;
    private final IssueRepository issueRepository;
    private final ProjectService projectService;
    private final ProjectAuthorizationService authorizationService;
    private final IssueConfigurationMapper mapper;

    // ── Types ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<IssueTypeConfigurationResponse> getTypes(UUID projectId, UUID principalId) {
        authorizationService.getMemberOrThrow(principalId, projectId);

        return typeRepository.findByProject_Id(projectId).stream().map(mapper::toTypeResponse).toList();
    }

    @Transactional
    public IssueTypeConfigurationResponse createType(IssueTypeRequest request, UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        if (typeRepository.existsByNameAndProject_Id(request.name(), projectId)) {
            throw new ResourceConflictException("A type named '" + request.name() + "' already exists");
        }

        if (request.isDefault()) {
            clearDefaultType(projectId);
        }

        IssueTypeConfiguration configuration = mapper.toTypeEntity(request);

        configuration.setProject(projectService.findProject(projectId));

        return mapper.toTypeResponse(typeRepository.save(configuration));
    }

    @Transactional
    public IssueTypeConfigurationResponse updateType(
            IssueTypeRequest request,
            UUID typeId,
            UUID projectId,
            UUID principalId
    ) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        IssueTypeConfiguration configuration = findType(typeId, projectId);

        if (request.name() != null
                && !configuration.getName().equals(request.name())
                && typeRepository.existsByNameAndProject_Id(request.name(), projectId)) {
            throw new ResourceConflictException("A type named '" + request.name() + "' already exists");
        }

        if (request.isDefault() && !configuration.isDefault()) {
            clearDefaultType(projectId);
        }

        mapper.updateTypeFromRequest(request, configuration);

        return mapper.toTypeResponse(typeRepository.save(configuration));
    }

    @Transactional
    public void deleteType(UUID typeId, UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        IssueTypeConfiguration configuration = findType(typeId, projectId);

        if (typeRepository.countByProject_Id(projectId) <= 1) {
            throw new BusinessRuleViolationException("Cannot delete the only issue type in this project");
        }

        if (issueRepository.existsByType_Id(typeId)) {
            throw new BusinessRuleViolationException("Cannot delete a type that is in use — reassign issues first");
        }

        typeRepository.delete(configuration);

        log.info("Deleted issue type {} from project {}", typeId, projectId);
    }

    // ── Priorities ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<IssuePriorityConfigurationResponse> getPriorities(UUID projectId, UUID principalId) {
        authorizationService.getMemberOrThrow(principalId, projectId);

        return priorityRepository.findByProject_IdOrderByOrderIndexAsc(projectId)
                .stream().map(mapper::toPriorityResponse).toList();
    }

    @Transactional
    public IssuePriorityConfigurationResponse createPriority(
            IssuePriorityRequest request,
            UUID projectId,
            UUID principalId
    ) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        if (priorityRepository.existsByNameAndProject_Id(request.name(), projectId)) {
            throw new ResourceConflictException("A priority named '" + request.name() + "' already exists");
        }

        if (request.isDefault()) {
            clearDefaultPriority(projectId);
        }

        IssuePriorityConfiguration configuration = mapper.toPriorityEntity(request);

        configuration.setProject(projectService.findProject(projectId));

        return mapper.toPriorityResponse(priorityRepository.save(configuration));
    }

    @Transactional
    public IssuePriorityConfigurationResponse updatePriority(
            IssuePriorityRequest request,
            UUID priorityId,
            UUID projectId,
            UUID principalId
    ) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        IssuePriorityConfiguration configuration = findPriority(priorityId, projectId);

        if (request.name() != null
                && !configuration.getName().equals(request.name())
                && priorityRepository.existsByNameAndProject_Id(request.name(), projectId)) {
            throw new ResourceConflictException("A priority named '" + request.name() + "' already exists");
        }

        if (request.isDefault() && !configuration.isDefault()) {
            clearDefaultPriority(projectId);
        }

        mapper.updatePriorityFromRequest(request, configuration);

        return mapper.toPriorityResponse(priorityRepository.save(configuration));
    }

    @Transactional
    public void deletePriority(UUID priorityId, UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        IssuePriorityConfiguration configuration = findPriority(priorityId, projectId);

        if (priorityRepository.countByProject_Id(projectId) <= 1) {
            throw new BusinessRuleViolationException("Cannot delete the only priority in this project");
        }

        if (issueRepository.existsByPriority_Id(priorityId)) {
            throw new BusinessRuleViolationException("Cannot delete a priority that is in use — reassign issues first");
        }

        priorityRepository.delete(configuration);

        log.info("Deleted issue priority {} from project {}", priorityId, projectId);
    }

    // ── Statuses ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<IssueStatusConfigurationResponse> getStatuses(UUID projectId, UUID principalId) {
        authorizationService.getMemberOrThrow(principalId, projectId);

        return statusRepository.findByProject_IdOrderByOrderIndexAsc(projectId)
                .stream().map(mapper::toStatusResponse).toList();
    }

    @Transactional
    public IssueStatusConfigurationResponse createStatus(
            IssueStatusRequest request,
            UUID projectId,
            UUID principalId
    ) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        if (statusRepository.existsByNameAndProject_Id(request.name(), projectId)) {
            throw new ResourceConflictException("A status named '" + request.name() + "' already exists");
        }

        if (request.isDefault()) {
            clearDefaultStatus(projectId);
        }

        IssueStatusConfiguration configuration = mapper.toStatusEntity(request);

        configuration.setProject(projectService.findProject(projectId));

        return mapper.toStatusResponse(statusRepository.save(configuration));
    }

    @Transactional
    public IssueStatusConfigurationResponse updateStatus(
            IssueStatusRequest request,
            UUID statusId,
            UUID projectId,
            UUID principalId
    ) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        IssueStatusConfiguration configuration = findStatus(statusId, projectId);

        if (request.name() != null
                && !configuration.getName().equals(request.name())
                && statusRepository.existsByNameAndProject_Id(request.name(), projectId)) {
            throw new ResourceConflictException("A status named '" + request.name() + "' already exists");
        }

        if (!request.isCompleted() && configuration.isCompleted()
                && statusRepository.countByProject_IdAndIsCompletedTrue(projectId) <= 1) {
            throw new BusinessRuleViolationException(
                    "At least one status must be marked as completed for sprint closure to work");
        }

        if (request.isDefault() && !configuration.isDefault()) {
            clearDefaultStatus(projectId);
        }

        mapper.updateStatusFromRequest(request, configuration);

        return mapper.toStatusResponse(statusRepository.save(configuration));
    }

    @Transactional
    public void deleteStatus(UUID statusId, UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        IssueStatusConfiguration configuration = findStatus(statusId, projectId);

        if (statusRepository.countByProject_Id(projectId) <= 1) {
            throw new BusinessRuleViolationException("Cannot delete the only status in this project");
        }

        if (configuration.isCompleted()
                && statusRepository.countByProject_IdAndIsCompletedTrue(projectId) <= 1) {
            throw new BusinessRuleViolationException(
                    "Cannot delete the last completed status — sprint closure requires at least one");
        }

        if (issueRepository.existsByStatus_Id(statusId)) {
            throw new BusinessRuleViolationException(
                    "Cannot delete a status that is in use — move issues to another status first");
        }

        statusRepository.delete(configuration);

        log.info("Deleted issue status {} from project {}", statusId, projectId);
    }

    // ── Lookup / default helpers (used by IssueService) ────────────────────

    public IssueTypeConfiguration getDefaultType(UUID projectId) {
        return typeRepository.findByProject_IdAndIsDefaultTrue(projectId)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "No default issue type configured for this project"));
    }

    public IssuePriorityConfiguration getDefaultPriority(UUID projectId) {
        return priorityRepository.findByProject_IdAndIsDefaultTrue(projectId)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "No default priority configured for this project"));
    }

    public IssueStatusConfiguration getDefaultStatus(UUID projectId) {
        return statusRepository.findByProject_IdAndIsDefaultTrue(projectId)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "No default status configured for this project"));
    }

    public IssueTypeConfiguration findType(UUID typeId, UUID projectId) {
        IssueTypeConfiguration configuration = typeRepository.findById(typeId)
                .orElseThrow(() -> new EntityNotFoundException("Issue type not found"));

        if (!configuration.getProject().getId().equals(projectId)) {
            throw new AccessDeniedException("Issue type does not belong to this project");
        }

        return configuration;
    }

    public IssuePriorityConfiguration findPriority(UUID priorityId, UUID projectId) {
        IssuePriorityConfiguration configuration = priorityRepository.findById(priorityId)
                .orElseThrow(() -> new EntityNotFoundException("Priority not found"));

        if (!configuration.getProject().getId().equals(projectId)) {
            throw new AccessDeniedException("Priority does not belong to this project");
        }

        return configuration;
    }

    public IssueStatusConfiguration findStatus(UUID statusId, UUID projectId) {
        IssueStatusConfiguration configuration = statusRepository.findById(statusId)
                .orElseThrow(() -> new EntityNotFoundException("Status not found"));

        if (!configuration.getProject().getId().equals(projectId)) {
            throw new AccessDeniedException("Status does not belong to this project");
        }

        return configuration;
    }

    // ── Seed defaults (called by ProjectService on project creation) ───────

    public void seedDefaults(Project project) {
        typeRepository.saveAll(List.of(
                buildType(project, "Bug",   true),
                buildType(project, "Task",  false),
                buildType(project, "Story", false)
        ));

        priorityRepository.saveAll(List.of(
                buildPriority(project, "Critical", 0, false),
                buildPriority(project, "High",     1, false),
                buildPriority(project, "Medium",   2, true),
                buildPriority(project, "Low",      3, false)
        ));

        statusRepository.saveAll(List.of(
                buildStatus(project, "To Do",       0, false, true),
                buildStatus(project, "In Progress", 1, false, false),
                buildStatus(project, "Done",        2, true,  false)
        ));
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private void clearDefaultType(UUID projectId) {
        typeRepository.findByProject_IdAndIsDefaultTrue(projectId)
                .ifPresent(c -> { c.setDefault(false); typeRepository.save(c); });
    }

    private void clearDefaultPriority(UUID projectId) {
        priorityRepository.findByProject_IdAndIsDefaultTrue(projectId)
                .ifPresent(c -> { c.setDefault(false); priorityRepository.save(c); });
    }

    private void clearDefaultStatus(UUID projectId) {
        statusRepository.findByProject_IdAndIsDefaultTrue(projectId)
                .ifPresent(c -> { c.setDefault(false); statusRepository.save(c); });
    }

    private IssueTypeConfiguration buildType(Project p, String name, boolean isDefault) {
        IssueTypeConfiguration c = new IssueTypeConfiguration();
        c.setProject(p);
        c.setName(name);
        c.setDefault(isDefault);

        return c;
    }

    private IssuePriorityConfiguration buildPriority(Project p, String name, int order, boolean isDefault) {
        IssuePriorityConfiguration c = new IssuePriorityConfiguration();
        c.setProject(p);
        c.setName(name);
        c.setOrderIndex(order);
        c.setDefault(isDefault);

        return c;
    }

    private IssueStatusConfiguration buildStatus(
            Project p,
            String name,
            int order,
            boolean isCompleted,
            boolean isDefault
    ) {
        IssueStatusConfiguration c = new IssueStatusConfiguration();
        c.setProject(p);
        c.setName(name);
        c.setOrderIndex(order);
        c.setCompleted(isCompleted);
        c.setDefault(isDefault);

        return c;
    }
}
