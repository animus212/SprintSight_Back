package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.IssueRequest;
import com.example.sprintsight.dtos.responses.ComponentResponse;
import com.example.sprintsight.dtos.responses.ComponentSummaryResponse;
import com.example.sprintsight.dtos.responses.IssueResponse;
import com.example.sprintsight.dtos.responses.IssueSummaryResponse;
import com.example.sprintsight.entities.Component;
import com.example.sprintsight.entities.Issue;
import org.mapstruct.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = { UserMapper.class, ProjectMapper.class, ComponentMapper.class, IssueConfigurationMapper.class },
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface IssueMapper {
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "project",     ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "assignedTo",  ignore = true)
    @Mapping(target = "components",  ignore = true)
    @Mapping(target = "type",        ignore = true)
    @Mapping(target = "priority",    ignore = true)
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    Issue toEntity(IssueRequest request);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "project",    ignore = true)
    @Mapping(target = "createdBy",  ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "components", ignore = true)
    @Mapping(target = "type",       ignore = true)
    @Mapping(target = "priority",   ignore = true)
    @Mapping(target = "status",     ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    void updateIssueFromRequest(IssueRequest request, @MappingTarget Issue issue);

    @Mapping(target = "components", source = "components", qualifiedByName = "sortedComponents")
    IssueResponse toIssueResponse(Issue issue);

    @Mapping(target = "components", source = "components", qualifiedByName = "toComponentSummaryList")
    IssueSummaryResponse toIssueSummaryResponse(Issue issue);

    @Named("toComponentSummary")
    default ComponentSummaryResponse toComponentSummary(Component c) {
        return new ComponentSummaryResponse(c.getId(), c.getName(),c.getDescription());
    }

    @Named("toComponentSummaryList")
    default Set<ComponentSummaryResponse> toComponentSummaryList(Set<Component> components) {
        return components.stream()
                .map(this::toComponentSummary)
                .collect(Collectors.toSet());
    }

    @Named("sortedComponents")
    default List<ComponentResponse> mapComponentsSorted(Set<Component> components) {
        if (components == null) return List.of();

        ComponentMapper cm = org.mapstruct.factory.Mappers.getMapper(ComponentMapper.class);

        return components.stream()
                .sorted(Comparator.comparing(Component::getName, String.CASE_INSENSITIVE_ORDER))
                .map(cm::toComponentResponse)
                .toList();
    }

    @Mapping(target = "components", source = "components", qualifiedByName = "sortedComponents")
    List<IssueResponse> toIssueResponses(List<Issue> issues);
}
