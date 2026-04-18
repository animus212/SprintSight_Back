package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.CreateIssueRequest;
import com.example.sprintsight.dtos.requests.UpdateIssueRequest;
import com.example.sprintsight.dtos.responses.IssueResponse;
import com.example.sprintsight.dtos.responses.IssueSummaryResponse;
import com.example.sprintsight.entities.*;
import com.example.sprintsight.mappers.IssueMapper;
import com.example.sprintsight.repositories.ComponentRepository;
import com.example.sprintsight.repositories.IssueEventRepository;
import com.example.sprintsight.repositories.IssueRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;
    private final IssueEventRepository issueEventRepository;
    private final ComponentRepository componentRepository;
    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectAuthorizationService authorizationService;
    private final IssueMapper issueMapper;

    @Transactional(readOnly = true)
    public IssueResponse getIssue(UUID issueId, UUID principalId) {
        Issue issue = findIssue(issueId);

        authorizationService.getMemberOrThrow(principalId, issue.getProject().getId());

        return issueMapper.toIssueResponse(issue);
    }

    @Transactional(readOnly = true)
    public List<IssueSummaryResponse> getProjectIssues(UUID projectId, UUID principalId) {
        authorizationService.getMemberOrThrow(principalId, projectId);

        return issueRepository.findByProject_Id(projectId)
                .stream()
                .map(issueMapper::toIssueSummaryResponse)
                .toList();
    }

    @Transactional
    public IssueResponse createIssue(
            CreateIssueRequest request,
            UUID projectId,
            UUID principalId
    ) {
        authorizationService.requireAnyRole(principalId, projectId, ProjectRole.PRODUCT_OWNER,
                ProjectRole.SCRUM_MASTER, ProjectRole.DEVELOPER);

        Project project = projectService.findProject(projectId);
        User creator = userService.findUser(principalId);

        Issue issue = issueMapper.toEntity(request);
        issue.setProject(project);
        issue.setCreatedBy(creator);

        if (request.assignedTo() != null) {
            authorizationService.requireAnyRole(principalId, projectId, ProjectRole.PRODUCT_OWNER,
                    ProjectRole.SCRUM_MASTER);

            issue.setAssignedTo(userService.findUser(request.assignedTo()));
        }

        if (request.componentIds() != null && !request.componentIds().isEmpty()) {
            issue.setComponents(resolveComponents(request.componentIds(), projectId));
        }

        Issue saved = issueRepository.save(issue);

        log.info("Created issue {} in project {}", saved.getId(), projectId);

        return issueMapper.toIssueResponse(saved);
    }

    @Transactional
    public IssueResponse updateIssue(
            UpdateIssueRequest request,
            UUID issueId,
            UUID principalId
    ) {
        Issue issue = findIssue(issueId);
        UUID projectId = issue.getProject().getId();

        ProjectRole role = authorizationService.getRoleOrThrow(principalId, projectId);

        boolean isPrivileged = role == ProjectRole.PRODUCT_OWNER || role == ProjectRole.SCRUM_MASTER;
        boolean isCreator = issue.getCreatedBy().getId().equals(principalId);

        if (!isPrivileged && !isCreator) {
            throw new AccessDeniedException("You can only edit issues you created");
        }

        recordChanges(issue, request, principalId);
        issueMapper.updateIssueFromRequest(request, issue);

        if (request.assignedTo() != null) {
            if (!isPrivileged) {
                throw new AccessDeniedException("Only product owners and scrum masters can assign issues");
            }

            issue.setAssignedTo(userService.findUser(request.assignedTo()));
        }

        if (request.priority() != null && !isPrivileged) {
            throw new AccessDeniedException("Only product owners and scrum masters can change priority");
        }

        if (request.componentIds() != null) {
            issue.setComponents(resolveComponents(request.componentIds(), projectId));
        }

        Issue saved = issueRepository.save(issue);

        log.info("Updated issue {}", issueId);

        return issueMapper.toIssueResponse(saved);
    }

    @Transactional
    public void deleteIssue(UUID issueId, UUID principalId) {
        Issue issue = findIssue(issueId);

        authorizationService.requireAnyRole(principalId, issue.getProject().getId(), ProjectRole.PRODUCT_OWNER,
                ProjectRole.SCRUM_MASTER);

        issueRepository.delete(issue);

        log.info("Deleted issue {}", issueId);
    }

    public Issue findIssue(UUID issueId) {
        return issueRepository.findById(issueId).orElseThrow(() -> new EntityNotFoundException("Issue not found"));
    }

    private Set<Component> resolveComponents(Set<UUID> componentIds, UUID projectId) {
        return componentIds.stream()
                .map(componentId -> componentRepository.findById(componentId)
                        .filter(c -> c.getProject().getId().equals(projectId))
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Component " + componentId + " not found in this project")))
                .collect(Collectors.toSet());
    }

    private void recordChanges(Issue issue, UpdateIssueRequest request, UUID principalId) {
        User changer = userService.findUser(principalId);
        List<IssueEvent> events = new ArrayList<>();

        if (!request.title().equals(issue.getTitle())) {
            events.add(buildEvent(issue, changer, "title", issue.getTitle(), request.title()));
        }

        if (!request.description().equals(issue.getDescription())) {
            events.add(buildEvent(issue, changer, "description", issue.getDescription(), request.description()));
        }

        if (request.type() != issue.getType()) {
            events.add(buildEvent(issue, changer, "type", issue.getType().name(), request.type().name()));
        }

        if (request.priority() != issue.getPriority()) {
            events.add(buildEvent(issue, changer, "priority", issue.getPriority().name(),
                    request.priority().name()));
        }

        if (request.status() != issue.getStatus()) {
            events.add(buildEvent(issue, changer, "status", issue.getStatus().name(), request.status().name()));
        }

        if (!Objects.equals(request.storyPoints(), issue.getStoryPoints())) {
            events.add(buildEvent(issue, changer, "storyPoints", issue.getStoryPoints().toString(),
                    request.storyPoints().toString()));
        }

        if (!request.fixVersion().equals(issue.getFixVersion())) {
            events.add(buildEvent(issue, changer, "fixVersion", issue.getFixVersion(), request.fixVersion()));
        }

        UUID current = issue.getAssignedTo() != null ? issue.getAssignedTo().getId() : null;
        UUID updated = request.assignedTo();

        if (!Objects.equals(current, updated)) {
            events.add(buildEvent(issue, changer, "assignedTo", current != null ? current.toString() : "null",
                    updated != null ? updated.toString() : "null"));
        }

        if (!events.isEmpty()) {
            issueEventRepository.saveAll(events);
        }
    }

    private IssueEvent buildEvent(Issue issue, User changer, String field, String oldVal, String newVal) {
        IssueEvent event = new IssueEvent();
        event.setIssue(issue);
        event.setChangedBy(changer);
        event.setFieldName(field);
        event.setOldValue(oldVal);
        event.setNewValue(newVal);

        return event;
    }
}
