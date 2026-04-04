package com.example.sprintsight.dtos.requests;

import com.example.sprintsight.entities.InvitationStatus;

import java.util.UUID;



public record Invitation (
     AddProjectMemberRequest projectMemberRequest,
     UUID projectId,
     UUID senderId,
     String projectName,
     String message,
     InvitationStatus status)
{}
