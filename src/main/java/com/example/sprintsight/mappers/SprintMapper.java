package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.SprintRequest;
import com.example.sprintsight.dtos.responses.SprintSummaryResponse;
import com.example.sprintsight.entities.Sprint;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {IssueMapper.class, ProjectMapper.class})
public interface SprintMapper {
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "project",     ignore = true)
    @Mapping(target = "status",      constant = "PLANNING")
    @Mapping(target = "startDate",   ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    Sprint toEntity(SprintRequest request);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "project",     ignore = true)
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "startDate",   ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    void updateSprintFromRequest(SprintRequest request, @MappingTarget Sprint sprint);

    SprintSummaryResponse toSprintSummaryResponse(Sprint sprint);
}
