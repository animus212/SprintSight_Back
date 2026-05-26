package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.ComponentRequest;
import com.example.sprintsight.dtos.responses.ComponentResponse;
import com.example.sprintsight.entities.Component;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ComponentMapper {
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "project",   ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Component toEntity(ComponentRequest request);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "project",   ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateComponentFromRequest(ComponentRequest request, @MappingTarget Component component);

    ComponentResponse toComponentResponse(Component component);
}
