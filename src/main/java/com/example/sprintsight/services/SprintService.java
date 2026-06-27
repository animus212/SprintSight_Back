package com.example.sprintsight.services;

import com.example.sprintsight.clients.PredictionServiceClient;
import com.example.sprintsight.dtos.requests.CloseSprintRequest;
import com.example.sprintsight.dtos.requests.SprintRequest;
import com.example.sprintsight.dtos.requests.StartSprintRequest;
import com.example.sprintsight.dtos.responses.PredictionResponse;
import com.example.sprintsight.dtos.responses.SprintIssueResponse;
import com.example.sprintsight.dtos.responses.SprintResponse;
import com.example.sprintsight.dtos.responses.SprintSummaryResponse;
import com.example.sprintsight.entities.*;
import com.example.sprintsight.exceptions.BusinessRuleViolationException;
import com.example.sprintsight.exceptions.ResourceConflictException;
import com.example.sprintsight.mappers.SprintMapper;
import com.example.sprintsight.repositories.IssueRepository;
import com.example.sprintsight.repositories.SprintIssueRepository;
import com.example.sprintsight.repositories.SprintRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SprintService {
    private final SprintRepository sprintRepository;
    private final SprintIssueRepository sprintIssueRepository;
    private final IssueRepository issueRepository;
    private final ProjectService projectService;
    private final ProjectAuthorizationService authorizationService;
    private final SprintMapper sprintMapper;
    private final PredictionServiceClient predictionServiceClient;

    @Transactional(readOnly = true)
    public List<SprintSummaryResponse> getSprints(UUID projectId, UUID principalId) {
        authorizationService.getMemberOrThrow(principalId, projectId);

        return sprintRepository.findByProject_Id(projectId)
                .stream()
                .map(sprintMapper::toSprintSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SprintResponse getSprint(UUID sprintId, UUID principalId) {
        Sprint sprint = findSprint(sprintId);

        authorizationService.getMemberOrThrow(principalId, sprint.getProject().getId());

        return buildSprintResponse(sprint);
    }

    @Transactional
    public SprintSummaryResponse createSprint(SprintRequest request, UUID projectId, UUID principalId) {
        authorizationService.requireAnyRole(principalId, projectId, ProjectRole.SCRUM_MASTER);

        Sprint sprint = sprintMapper.toEntity(request);
        sprint.setProject(projectService.findProject(projectId));
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());

        Sprint saved = sprintRepository.save(sprint);

        log.info("Created sprint {} in project {}", saved.getId(), projectId);

        return sprintMapper.toSprintSummaryResponse(saved);
    }

    @Transactional
    public SprintSummaryResponse updateSprint(SprintRequest request, UUID sprintId, UUID principalId) {
        Sprint sprint = findSprint(sprintId);

        authorizationService.requireAnyRole(principalId, sprint.getProject().getId(), ProjectRole.SCRUM_MASTER);

        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Cannot update a completed sprint");
        }

        sprintMapper.updateSprintFromRequest(request, sprint);
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());

        return sprintMapper.toSprintSummaryResponse(sprintRepository.save(sprint));
    }

    @Transactional
    public SprintResponse startSprint(StartSprintRequest request, UUID sprintId, UUID principalId) {
        Sprint sprint = findSprint(sprintId);
        UUID projectId = sprint.getProject().getId();

        authorizationService.requireAnyRole(principalId, projectId, ProjectRole.SCRUM_MASTER);

        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new BusinessRuleViolationException("Only a sprint in PLANNING can be started");
        }

        if (sprintRepository.existsByProject_IdAndStatus(projectId, SprintStatus.ACTIVE)) {
            throw new ResourceConflictException(
                    "A sprint is already active in this project — complete it before starting another");
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());

        Sprint saved = sprintRepository.save(sprint);

        log.info("Started sprint {}", sprintId);

        return buildSprintResponse(saved);
    }

    @Transactional
    public SprintResponse closeSprint(CloseSprintRequest request, UUID sprintId, UUID principalId) {
        Sprint sprint = findSprint(sprintId);

        authorizationService.requireAnyRole(principalId, sprint.getProject().getId(), ProjectRole.SCRUM_MASTER);

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BusinessRuleViolationException("Only an active sprint can be closed");
        }

        List<SprintIssue> activeEntries = sprintIssueRepository.findBySprint_IdAndRemovedAtIsNull(sprintId);

        for (SprintIssue entry : activeEntries) {
            entry.setStatusAtClosure(entry.getIssue().getStatus());
        }

        sprintIssueRepository.saveAll(activeEntries);

        List<SprintIssue> unfinished = activeEntries.stream()
                .filter(e -> !e.getStatusAtClosure().isCompleted())
                .toList();

        if (!unfinished.isEmpty()) {
            handleUnfinishedIssues(unfinished, request.moveUnfinishedToSprintId(), sprint.getProject().getId());
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setCompletedAt(Instant.now());

        Sprint saved = sprintRepository.save(sprint);

        long completed = activeEntries.stream().filter(e -> e.getStatusAtClosure().isCompleted()).count();

        log.info("Closed sprint {} — {} issues completed, {} unfinished", sprintId, completed, unfinished.size());

        return buildSprintResponse(saved);
    }

    @Transactional
    public SprintIssueResponse addIssueToSprint(UUID sprintId, UUID issueId, UUID principalId) {
        Sprint sprint = findSprint(sprintId);

        authorizationService.requireAnyRole(principalId, sprint.getProject().getId(),
                ProjectRole.SCRUM_MASTER, ProjectRole.PRODUCT_OWNER);

        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Cannot add issues to a completed sprint");
        }

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Issue not found"));

        if (!issue.getProject().getId().equals(sprint.getProject().getId())) {
            throw new IllegalArgumentException("Issue does not belong to this project");
        }

        if (sprintIssueRepository.existsBySprint_IdAndIssue_IdAndRemovedAtIsNull(sprintId, issueId)) {
            throw new ResourceConflictException("Issue is already in this sprint");
        }

        SprintIssue entry = new SprintIssue();
        entry.setSprint(sprint);
        entry.setIssue(issue);

        SprintIssue saved = sprintIssueRepository.save(entry);

        log.info("Added issue {} to sprint {}", issueId, sprintId);

        return sprintMapper.toSprintIssueResponse(saved, sprint);
    }

    @Transactional
    public void removeIssueFromSprint(UUID sprintId, UUID issueId, UUID principalId) {
        Sprint sprint = findSprint(sprintId);

        authorizationService.requireAnyRole(principalId, sprint.getProject().getId(),
                ProjectRole.SCRUM_MASTER, ProjectRole.PRODUCT_OWNER);

        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Cannot remove issues from a completed sprint");
        }

        SprintIssue entry = sprintIssueRepository.findBySprint_IdAndIssue_IdAndRemovedAtIsNull(sprintId, issueId)
                .orElseThrow(() -> new EntityNotFoundException("Issue is not currently in this sprint"));

        entry.setRemovedAt(Instant.now());

        sprintIssueRepository.save(entry);

        log.info("Removed issue {} from sprint {}", issueId, sprintId);
    }

    public PredictionResponse predict(UUID sprintId) {
        String featuresJson = sprintRepository.getSprintFeatures(sprintId);

        return predictionServiceClient.getPrediction(sprintId, featuresJson);
    }

    public Sprint findSprint(UUID sprintId) {
        return sprintRepository.findById(sprintId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found"));
    }

    private void handleUnfinishedIssues(
            List<SprintIssue> unfinished,
            UUID targetSprintId,
            UUID projectId
    ) {
        if (targetSprintId == null) {
            log.info("{} unfinished issues moved to backlog", unfinished.size());

            return;
        }

        Sprint targetSprint = findSprint(targetSprintId);

        if (!targetSprint.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Target sprint does not belong to this project");
        }

        if (targetSprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Cannot move issues to a completed sprint");
        }

        List<SprintIssue> newEntries = unfinished.stream().map(entry -> {
            SprintIssue newEntry = new SprintIssue();
            newEntry.setSprint(targetSprint);
            newEntry.setIssue(entry.getIssue());

            return newEntry;
        }).toList();

        sprintIssueRepository.saveAll(newEntries);

        log.info("Moved {} unfinished issues to sprint {}", unfinished.size(), targetSprintId);
    }

    private SprintResponse buildSprintResponse(Sprint sprint) {
        List<SprintIssue> allEntries = sprintIssueRepository.findBySprint_Id(sprint.getId());

        List<SprintIssueResponse> issueResponses = allEntries
                .stream()
                .map(e -> sprintMapper.toSprintIssueResponse(e, sprint))
                .toList();

        long activeCount = issueResponses.stream().filter(r -> r.removedAt() == null).count();
        long completedCount = issueResponses.stream()
                .filter(r -> r.removedAt() == null && r.completedInSprint()).count();
        long addedAfterStartCt = issueResponses.stream()
                .filter(r -> r.removedAt() == null && r.addedAfterStart()).count();

        return sprintMapper.toSprintResponse(
                sprint,
                issueResponses,
                (int) activeCount,
                (int) completedCount,
                (int) addedAfterStartCt
        );
    }
}
