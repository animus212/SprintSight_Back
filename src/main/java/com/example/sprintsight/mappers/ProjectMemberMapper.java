package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.AddProjectMemberRequest;
import com.example.sprintsight.dtos.requests.UpdateProjectMemberRequest;
import com.example.sprintsight.dtos.responses.ProjectMemberResponse;
import com.example.sprintsight.entities.ProjectMember;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

public interface ProjectMemberMapper {

    ProjectMember toEntity(AddProjectMemberRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProjectMemberFromPatch(UpdateProjectMemberRequest request, @MappingTarget ProjectMember projectMember);

    ProjectMemberResponse toProjectMemberResponse(ProjectMember savedProject);

    void updateProjectMemberFromPut(UpdateProjectMemberRequest request, ProjectMember projectMember);
}
