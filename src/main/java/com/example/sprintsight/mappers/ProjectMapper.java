package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.CreateProjectRequest;
import com.example.sprintsight.dtos.requests.UpdateProjectRequest;
import com.example.sprintsight.dtos.responses.ProjectResponse;
import com.example.sprintsight.entities.Project;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    Project toEntity(CreateProjectRequest request);
    
    void updateProjectFromPut(UpdateProjectRequest request, @MappingTarget Project project);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProjectFromPatch(UpdateProjectRequest request, @MappingTarget Project project);

    ProjectResponse toProjectResponse(Project project);
}
