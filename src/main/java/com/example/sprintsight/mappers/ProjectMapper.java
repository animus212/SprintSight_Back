package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.ProjectRequest;
import com.example.sprintsight.dtos.responses.ProjectResponse;
import com.example.sprintsight.dtos.responses.ProjectSummaryResponse;
import com.example.sprintsight.entities.Project;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = UserMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProjectMapper {
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    @Mapping(target = "members",     ignore = true)
    @Mapping(target = "invitations", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    Project toEntity(ProjectRequest request);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    @Mapping(target = "members",     ignore = true)
    @Mapping(target = "invitations", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    void updateProjectFromRequest(ProjectRequest request, @MappingTarget Project project);

    ProjectResponse toProjectResponse(Project project);

    ProjectSummaryResponse toProjectSummaryResponse(Project project);
}
