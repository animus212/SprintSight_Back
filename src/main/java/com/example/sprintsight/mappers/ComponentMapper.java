package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.ComponentRequest;
import com.example.sprintsight.dtos.responses.ComponentResponse;
import com.example.sprintsight.entities.Component;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ComponentMapper {
    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "project", ignore = true)
    Component toEntity(ComponentRequest request);

    @Mapping(target = "id",      ignore = true)
    @Mapping(target = "project", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateComponentFromRequest(ComponentRequest request, @MappingTarget Component component);

    ComponentResponse toComponentResponse(Component component);
}
