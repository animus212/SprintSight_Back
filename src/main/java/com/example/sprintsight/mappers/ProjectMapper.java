package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.CreateProjectRequest;
import com.example.sprintsight.dtos.requests.UpdateProjectRequest;
import com.example.sprintsight.dtos.responses.ProjectResponse;
import com.example.sprintsight.entities.Project;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ProjectMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project toEntity(CreateProjectRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProjectFromPut(UpdateProjectRequest request, @MappingTarget Project project);

    @InheritConfiguration(name = "updateProjectFromPut")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProjectFromPatch(UpdateProjectRequest request, @MappingTarget Project project);

    ProjectResponse toProjectResponse(Project project);
}
