package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.IssuePriorityRequest;
import com.example.sprintsight.dtos.requests.IssueStatusRequest;
import com.example.sprintsight.dtos.requests.IssueTypeRequest;
import com.example.sprintsight.dtos.responses.IssuePriorityConfigurationResponse;
import com.example.sprintsight.dtos.responses.IssueStatusConfigurationResponse;
import com.example.sprintsight.dtos.responses.IssueTypeConfigurationResponse;
import com.example.sprintsight.entities.*;
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
            throw new IllegalStateException("A type named '" + request.name() + "' already exists");
        }

        IssueTypeConfiguration Configuration = mapper.toTypeEntity(request);
        Configuration.setProject(projectService.findProject(projectId));

        if (request.isDefault()) {
            clearDefaultType(projectId);
        }

        return mapper.toTypeResponse(typeRepository.save(Configuration));
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

        IssueTypeConfiguration Configuration = findType(typeId, projectId);

        if (request.name() != null
                && typeRepository.existsByNameAndProject_Id(request.name(), projectId)
                && !Configuration.getName().equals(request.name())) {
            throw new IllegalStateException("A type named '" + request.name() + "' already exists");
        }

        if (request.isDefault()) {
            clearDefaultType(projectId);
        }

        mapper.updateTypeFromRequest(request, Configuration);
        return mapper.toTypeResponse(typeRepository.save(Configuration));
    }

    @Transactional
    public void deleteType(UUID typeId, UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        IssueTypeConfiguration Configuration = findType(typeId, projectId);

        if (!typeRepository.existsByProject_IdAndIdNot(projectId, typeId)) {
            throw new IllegalStateException("Cannot delete the only issue type in this project");
        }

        if (issueRepository.existsByType_Id(typeId)) {
            throw new IllegalStateException("Cannot delete a type that is in use — reassign issues first");
        }

        typeRepository.delete(Configuration);
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
            throw new IllegalStateException("A priority named '" + request.name() + "' already exists");
        }

        IssuePriorityConfiguration Configuration = mapper.toPriorityEntity(request);
        Configuration.setProject(projectService.findProject(projectId));

        if (request.isDefault()) {
            clearDefaultPriority(projectId);
        }

        return mapper.toPriorityResponse(priorityRepository.save(Configuration));
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

        IssuePriorityConfiguration Configuration = findPriority(priorityId, projectId);

        if (request.name() != null
                && priorityRepository.existsByNameAndProject_Id(request.name(), projectId)
                && !Configuration.getName().equals(request.name())) {
            throw new IllegalStateException("A priority named '" + request.name() + "' already exists");
        }

        if (request.isDefault()) {
            clearDefaultPriority(projectId);
        }

        mapper.updatePriorityFromRequest(request, Configuration);
        return mapper.toPriorityResponse(priorityRepository.save(Configuration));
    }

    @Transactional
    public void deletePriority(UUID priorityId, UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        IssuePriorityConfiguration Configuration = findPriority(priorityId, projectId);

        if (!priorityRepository.existsByProject_IdAndIdNot(projectId, priorityId)) {
            throw new IllegalStateException("Cannot delete the only priority in this project");
        }

        if (issueRepository.existsByPriority_Id(priorityId)) {
            throw new IllegalStateException("Cannot delete a priority that is in use — reassign issues first");
        }

        priorityRepository.delete(Configuration);
    }

    // ── Statuses ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<IssueStatusConfigurationResponse> getStatuses(UUID projectId, UUID principalId) {
        authorizationService.getMemberOrThrow(principalId, projectId);

        return statusRepository.findByProject_IdOrderByOrderIndexAsc(projectId)
                .stream().map(mapper::toStatusResponse).toList();
    }

    @Transactional
    public IssueStatusConfigurationResponse createStatus(IssueStatusRequest request, UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        if (statusRepository.existsByNameAndProject_Id(request.name(), projectId)) {
            throw new IllegalStateException("A status named '" + request.name() + "' already exists");
        }

        IssueStatusConfiguration Configuration = mapper.toStatusEntity(request);
        Configuration.setProject(projectService.findProject(projectId));

        if (request.isDefault()) {
            clearDefaultStatus(projectId);
        }

        return mapper.toStatusResponse(statusRepository.save(Configuration));
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

        IssueStatusConfiguration Configuration = findStatus(statusId, projectId);

        if (request.name() != null
                && statusRepository.existsByNameAndProject_Id(request.name(), projectId)
                && !Configuration.getName().equals(request.name())) {
            throw new IllegalStateException("A status named '" + request.name() + "' already exists");
        }

        if (!request.isCompleted() && Configuration.isCompleted()) {
            if (!statusRepository.existsByProject_IdAndIsCompletedTrue(projectId)) {
                throw new IllegalStateException(
                        "At least one status must be marked as completed for sprint closure to work");
            }
        }

        if (request.isDefault()) {
            clearDefaultStatus(projectId);
        }

        mapper.updateStatusFromRequest(request, Configuration);
        return mapper.toStatusResponse(statusRepository.save(Configuration));
    }

    @Transactional
    public void deleteStatus(UUID statusId, UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        IssueStatusConfiguration Configuration = findStatus(statusId, projectId);

        if (!statusRepository.existsByProject_IdAndIdNot(projectId, statusId)) {
            throw new IllegalStateException("Cannot delete the only status in this project");
        }

        if (Configuration.isCompleted()
                && !statusRepository.existsByProject_IdAndIsCompletedTrue(projectId)) {
            throw new IllegalStateException(
                    "Cannot delete the last completed status — sprint closure requires at least one");
        }

        if (issueRepository.existsByStatus_Id(statusId)) {
            throw new IllegalStateException(
                    "Cannot delete a status that is in use — move issues to another status first");
        }

        statusRepository.delete(Configuration);
    }

    // ── Default resolution (used by IssueService) ─────────────────────────

    public IssueTypeConfiguration getDefaultType(UUID projectId) {
        return typeRepository.findByProject_IdAndIsDefaultTrue(projectId)
                .orElseThrow(() -> new IllegalStateException(
                        "No default issue type Configured for this project"));
    }

    public IssuePriorityConfiguration getDefaultPriority(UUID projectId) {
        return priorityRepository.findByProject_IdAndIsDefaultTrue(projectId)
                .orElseThrow(() -> new IllegalStateException(
                        "No default priority Configured for this project"));
    }

    public IssueStatusConfiguration getDefaultStatus(UUID projectId) {
        return statusRepository.findByProject_IdAndIsDefaultTrue(projectId)
                .orElseThrow(() -> new IllegalStateException(
                        "No default status Configured for this project"));
    }

    public IssueTypeConfiguration findType(UUID typeId, UUID projectId) {
        IssueTypeConfiguration Configuration = typeRepository.findById(typeId)
                .orElseThrow(() -> new EntityNotFoundException("Issue type not found"));

        if (!Configuration.getProject().getId().equals(projectId)) {
            throw new AccessDeniedException("Issue type does not belong to this project");
        }

        return Configuration;
    }

    public IssuePriorityConfiguration findPriority(UUID priorityId, UUID projectId) {
        IssuePriorityConfiguration Configuration = priorityRepository.findById(priorityId)
                .orElseThrow(() -> new EntityNotFoundException("Priority not found"));

        if (!Configuration.getProject().getId().equals(projectId)) {
            throw new AccessDeniedException("Priority does not belong to this project");
        }

        return Configuration;
    }

    public IssueStatusConfiguration findStatus(UUID statusId, UUID projectId) {
        IssueStatusConfiguration Configuration = statusRepository.findById(statusId)
                .orElseThrow(() -> new EntityNotFoundException("Status not found"));

        if (!Configuration.getProject().getId().equals(projectId)) {
            throw new AccessDeniedException("Status does not belong to this project");
        }

        return Configuration;
    }

    // ── Default seeding (called by ProjectService on project creation) ─────

    public void seedDefaults(Project project) {
        List<IssueTypeConfiguration> types = List.of(
                buildType(project, "Bug",   true),
                buildType(project, "Task",  false),
                buildType(project, "Story", false)
        );
        typeRepository.saveAll(types);

        List<IssuePriorityConfiguration> priorities = List.of(
                buildPriority(project, "Critical", 0, false),
                buildPriority(project, "High",     1, false),
                buildPriority(project, "Medium",   2, true),
                buildPriority(project, "Low",      3, false)
        );
        priorityRepository.saveAll(priorities);

        List<IssueStatusConfiguration> statuses = List.of(
                buildStatus(project, "To Do",       0, false, true),
                buildStatus(project, "In Progress", 1, false, false),
                buildStatus(project, "Done",        2, true,  false)
        );
        statusRepository.saveAll(statuses);
    }

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
