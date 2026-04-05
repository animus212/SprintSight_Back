package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.UpdateProjectMemberRequest;
import com.example.sprintsight.dtos.responses.ProjectMemberResponse;
import com.example.sprintsight.entities.ProjectMember;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ProjectMemberMapper {
    @Mapping(target = "member", source = "user")
    ProjectMemberResponse toProjectMemberResponse(ProjectMember projectMember);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "joinedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProjectMemberFromPatch(UpdateProjectMemberRequest request, @MappingTarget ProjectMember projectMember);
}
