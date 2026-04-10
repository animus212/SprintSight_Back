package com.example.sprintsight.mappers;

import com.example.sprintsight.dtos.requests.SendInvitationRequest;
import com.example.sprintsight.dtos.responses.InvitationResponse;
import com.example.sprintsight.entities.ProjectInvitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface InvitationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "receiver", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "respondedAt", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    ProjectInvitation toEntity(SendInvitationRequest request);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    InvitationResponse toInvitationResponse(ProjectInvitation invitation);
}
