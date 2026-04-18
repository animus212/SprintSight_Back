package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.CreateIssueRequest;
import com.example.sprintsight.dtos.requests.UpdateIssueRequest;
import com.example.sprintsight.dtos.responses.IssueResponse;
import com.example.sprintsight.dtos.responses.IssueSummaryResponse;
import com.example.sprintsight.entities.Issue;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ProjectMapper.class, ComponentMapper.class})
public interface IssueMapper {

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "project",    ignore = true)
    @Mapping(target = "createdBy",  ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "components", ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    @Mapping(target = "status",     constant = "TODO")
    Issue toEntity(CreateIssueRequest request);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "project",   ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "assignedTo",ignore = true)
    @Mapping(target = "components",ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateIssueFromRequest(UpdateIssueRequest request, @MappingTarget Issue issue);

    @Mapping(target = "project",    source = "project")
    @Mapping(target = "createdBy",  source = "createdBy")
    @Mapping(target = "assignedTo", source = "assignedTo")
    @Mapping(target = "components", source = "components")
    IssueResponse toIssueResponse(Issue issue);

    IssueSummaryResponse toIssueSummaryResponse(Issue issue);
}
