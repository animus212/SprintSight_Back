package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.IssuePriorityRequest;
import com.example.sprintsight.dtos.requests.IssueStatusRequest;
import com.example.sprintsight.dtos.requests.IssueTypeRequest;
import com.example.sprintsight.dtos.responses.IssuePriorityConfigurationResponse;
import com.example.sprintsight.dtos.responses.IssueStatusConfigurationResponse;
import com.example.sprintsight.dtos.responses.IssueTypeConfigurationResponse;
import com.example.sprintsight.entities.IssuePriorityConfiguration;
import com.example.sprintsight.entities.IssueStatusConfiguration;
import com.example.sprintsight.entities.IssueTypeConfiguration;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IssueConfigurationMapper {
    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "project", ignore = true)
    IssueTypeConfiguration toTypeEntity(IssueTypeRequest request);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "project", ignore = true)
    IssuePriorityConfiguration toPriorityEntity(IssuePriorityRequest request);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "project", ignore = true)
    IssueStatusConfiguration toStatusEntity(IssueStatusRequest request);

    IssueTypeConfigurationResponse toTypeResponse(IssueTypeConfiguration Configuration);
    IssuePriorityConfigurationResponse toPriorityResponse(IssuePriorityConfiguration Configuration);
    IssueStatusConfigurationResponse toStatusResponse(IssueStatusConfiguration Configuration);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "project", ignore = true)
    void updateTypeFromRequest(IssueTypeRequest request, @MappingTarget IssueTypeConfiguration Configuration);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "project", ignore = true)
    void updatePriorityFromRequest(IssuePriorityRequest request,
                                   @MappingTarget IssuePriorityConfiguration Configuration);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "project", ignore = true)
    void updateStatusFromRequest(IssueStatusRequest request, @MappingTarget IssueStatusConfiguration Configuration);
}
