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

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface IssueConfigurationMapper {

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "default", source = "isDefault")
    IssueTypeConfiguration toTypeEntity(IssueTypeRequest request);

    @Mapping(target = "isDefault", source = "default")
    IssueTypeConfigurationResponse toTypeResponse(IssueTypeConfiguration configuration);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "default", source = "isDefault")
    void updateTypeFromRequest(IssueTypeRequest request, @MappingTarget IssueTypeConfiguration configuration);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "default", source = "isDefault")
    IssuePriorityConfiguration toPriorityEntity(IssuePriorityRequest request);

    @Mapping(target = "isDefault", source = "default")
    IssuePriorityConfigurationResponse toPriorityResponse(IssuePriorityConfiguration configuration);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "default", source = "isDefault")
    void updatePriorityFromRequest(IssuePriorityRequest request,
                                   @MappingTarget IssuePriorityConfiguration configuration);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "project",   ignore = true)
    @Mapping(target = "default",   source = "isDefault")
    @Mapping(target = "completed", source = "isCompleted")
    IssueStatusConfiguration toStatusEntity(IssueStatusRequest request);

    @Mapping(target = "isDefault",   source = "default")
    @Mapping(target = "isCompleted", source = "completed")
    IssueStatusConfigurationResponse toStatusResponse(IssueStatusConfiguration configuration);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "project",   ignore = true)
    @Mapping(target = "default",   source = "isDefault")
    @Mapping(target = "completed", source = "isCompleted")
    void updateStatusFromRequest(IssueStatusRequest request, @MappingTarget IssueStatusConfiguration configuration);
}
