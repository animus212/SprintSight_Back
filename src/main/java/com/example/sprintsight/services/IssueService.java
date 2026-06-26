package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.IssueRequest;
import com.example.sprintsight.dtos.responses.IssueResponse;
import com.example.sprintsight.dtos.responses.IssueSummaryResponse;
import com.example.sprintsight.entities.*;
import com.example.sprintsight.mappers.IssueMapper;
import com.example.sprintsight.repositories.ComponentRepository;
import com.example.sprintsight.repositories.IssueEventRepository;
import com.example.sprintsight.repositories.IssueRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private final IssueConfigurationService issueConfigurationService;
    private final IssueMapper issueMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public IssueResponse getIssue(UUID issueId, UUID principalId) {
        Issue issue = issueRepository.findWithDetailsById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Issue not found"));

        authorizationService.getMemberOrThrow(principalId, issue.getProject().getId());

        return issueMapper.toIssueResponse(issue);
    }

    @Transactional(readOnly = true)
    public List<IssueSummaryResponse> getBacklog(UUID projectId, UUID principalId) {
        authorizationService.getMemberOrThrow(principalId, projectId);

        return issueRepository.findBacklogByProject_Id(projectId)
                .stream()
                .map(issueMapper::toIssueSummaryResponse)
                .toList();
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
    public IssueResponse createIssue(IssueRequest request, UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId,
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER, ProjectRole.DEVELOPER);

        if (request.assignedTo() != null) {
            authorizationService.requireAnyRole(principalId, projectId,
                    ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);
        }

        Project project = projectService.findProject(projectId);
        User creator = userService.findUser(principalId);

        Issue issue = issueMapper.toEntity(request);
        issue.setProject(project);
        issue.setCreatedBy(creator);

        issue.setType(issueConfigurationService.findType(request.typeId(), projectId));
        issue.setPriority(issueConfigurationService.findPriority(request.priorityId(), projectId));
        issue.setStatus(request.statusId() != null
                ? issueConfigurationService.findStatus(request.statusId(), projectId)
                : issueConfigurationService.getDefaultStatus(projectId));

        if (request.assignedTo() != null) {
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
    public IssueResponse updateIssue(IssueRequest request, UUID issueId, UUID principalId) {
        Issue issue = issueRepository.findWithDetailsById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Issue not found"));
        UUID projectId = issue.getProject().getId();

        ProjectRole role = authorizationService.getRoleOrThrow(principalId, projectId);

        boolean isPrivileged = role == ProjectRole.PRODUCT_OWNER || role == ProjectRole.SCRUM_MASTER;
        boolean isCreator = issue.getCreatedBy() != null && issue.getCreatedBy().getId().equals(principalId);

        if (!isPrivileged && !isCreator) {
            throw new AccessDeniedException("You can only edit issues you created");
        }

        if (request.priorityId() != null && !isPrivileged) {
            throw new AccessDeniedException("Only product owners and scrum masters can change priority");
        }

        if (request.assignedTo() != null && !isPrivileged) {
            throw new AccessDeniedException("Only product owners and scrum masters can assign issues");
        }

        recordChanges(issue, request, principalId, projectId);

        issueMapper.updateIssueFromRequest(request, issue);

        if (request.typeId() != null) {
            issue.setType(issueConfigurationService.findType(request.typeId(), projectId));
        }

        if (request.priorityId() != null) {
            issue.setPriority(issueConfigurationService.findPriority(request.priorityId(), projectId));
        }

        if (request.statusId() != null) {
            issue.setStatus(issueConfigurationService.findStatus(request.statusId(), projectId));
        }

        if (request.assignedTo() != null) {
            issue.setAssignedTo(userService.findUser(request.assignedTo()));
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

        authorizationService.requireAnyRole(principalId, issue.getProject().getId(),
                ProjectRole.PRODUCT_OWNER, ProjectRole.SCRUM_MASTER);

        issueRepository.delete(issue);

        log.info("Deleted issue {}", issueId);
    }

    public Issue findIssue(UUID issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Issue not found"));
    }

    private Set<Component> resolveComponents(Set<UUID> componentIds, UUID projectId) {
        if (componentIds.isEmpty()) return new HashSet<>();

        Set<Component> found = componentRepository.findAllByIdInAndProject_Id(componentIds, projectId);

        if (found.size() != componentIds.size()) {
            Set<UUID> foundIds = found.stream().map(Component::getId).collect(java.util.stream.Collectors.toSet());

            UUID missing = componentIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElseThrow();

            throw new EntityNotFoundException("Component " + missing + " not found in this project");
        }

        return found;
    }

    private void recordChanges(Issue issue, IssueRequest request, UUID principalId, UUID projectId) {
        List<IssueEvent> events = new ArrayList<>();
        User changer = entityManager.getReference(User.class, principalId);

        if (request.title() != null && !Objects.equals(request.title(), issue.getTitle())) {
            events.add(buildEvent(issue, changer, IssueEventField.TITLE,
                    issue.getTitle(), request.title()));
        }

        if (!Objects.equals(request.description(), issue.getDescription())) {
            events.add(buildEvent(issue, changer, IssueEventField.DESCRIPTION,
                    issue.getDescription(), request.description()));
        }

        if (request.typeId() != null && !request.typeId().equals(issue.getType().getId())) {
            events.add(buildEvent(issue, changer, IssueEventField.TYPE,
                    issue.getType().getId().toString(), request.typeId().toString()));
        }

        if (request.priorityId() != null && !request.priorityId().equals(issue.getPriority().getId())) {
            events.add(buildEvent(issue, changer, IssueEventField.PRIORITY,
                    issue.getPriority().getId().toString(), request.priorityId().toString()));
        }

        if (request.statusId() != null && !request.statusId().equals(issue.getStatus().getId())) {
            events.add(buildEvent(issue, changer, IssueEventField.STATUS,
                    issue.getStatus().getId().toString(), request.statusId().toString()));
        }

        if (!Objects.equals(request.storyPoints(), issue.getStoryPoints())) {
            events.add(buildEvent(issue, changer, IssueEventField.STORY_POINTS,
                    asString(issue.getStoryPoints()), asString(request.storyPoints())));
        }

        if (!Objects.equals(request.fixVersion(), issue.getFixVersion())) {
            events.add(buildEvent(issue, changer, IssueEventField.FIX_VERSION,
                    issue.getFixVersion(), request.fixVersion()));
        }

        UUID currentAssignee = issue.getAssignedTo() != null ? issue.getAssignedTo().getId() : null;
        if (!Objects.equals(currentAssignee, request.assignedTo())) {
            events.add(buildEvent(issue, changer, IssueEventField.ASSIGNEE,
                    asString(currentAssignee), asString(request.assignedTo())));
        }

        if (!events.isEmpty()) {
            issueEventRepository.saveAll(events);
        }
    }

    private IssueEvent buildEvent(
            Issue issue,
            User changer,
            IssueEventField field,
            String oldVal,
            String newVal
    ) {
        IssueEvent event = new IssueEvent();
        event.setIssue(issue);
        event.setChangedBy(changer);
        event.setFieldName(field);
        event.setOldValue(oldVal);
        event.setNewValue(newVal);

        return event;
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
