package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.SprintRequest;
import com.example.sprintsight.dtos.responses.ProjectSummaryResponse;
import com.example.sprintsight.dtos.responses.SprintIssueResponse;
import com.example.sprintsight.dtos.responses.SprintResponse;
import com.example.sprintsight.dtos.responses.SprintSummaryResponse;
import com.example.sprintsight.entities.Sprint;
import com.example.sprintsight.entities.SprintIssue;
import org.mapstruct.*;

import java.time.ZoneOffset;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = IssueConfigurationMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SprintMapper {
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "project",     ignore = true)
    @Mapping(target = "status",      constant = "PLANNING")
    @Mapping(target = "startDate",   ignore = true)
    @Mapping(target = "endDate",     ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    Sprint toEntity(SprintRequest request);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "project",     ignore = true)
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "startDate",   ignore = true)
    @Mapping(target = "endDate",     ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    void updateSprintFromRequest(SprintRequest request, @MappingTarget Sprint sprint);

    SprintSummaryResponse toSprintSummaryResponse(Sprint sprint);

    @Mapping(target = "issue",            source = "entry.issue")
    @Mapping(target = "addedAt",          source = "entry.addedAt")
    @Mapping(target = "addedAfterStart",  expression = "java(computeAddedAfterStart(entry, sprint))")
    @Mapping(target = "statusAtClosure",  source = "entry.statusAtClosure")
    @Mapping(target = "completedInSprint",
            expression = "java(entry.getStatusAtClosure() != null && entry.getStatusAtClosure().isCompleted())")
    @Mapping(target = "removedAt",        source = "entry.removedAt")
    SprintIssueResponse toSprintIssueResponse(SprintIssue entry, @Context Sprint sprint);

    @Named("computeAddedAfterStart")
    default boolean computeAddedAfterStart(SprintIssue entry, Sprint sprint) {
        return sprint.getStartDate() != null
                && entry.getAddedAt().isAfter(sprint.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    default SprintResponse toSprintResponse(
            Sprint sprint,
            List<SprintIssueResponse> issues,
            int totalIssues,
            int completed,
            int addedAfterStart
    ) {
        return new SprintResponse(
                sprint.getId(),
                sprint.getName(),
                sprint.getGoal(),
                new ProjectSummaryResponse(sprint.getProject().getId(), sprint.getProject().getName(), sprint.getProject().getImageUrl()),
                sprint.getStatus(),
                sprint.getStartDate(),
                sprint.getEndDate(),
                sprint.getCompletedAt(),
                totalIssues,
                completed,
                addedAfterStart,
                issues
        );
    }
}
