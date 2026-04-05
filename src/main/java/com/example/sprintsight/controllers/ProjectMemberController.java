package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.SendInvitationRequest;
import com.example.sprintsight.dtos.requests.UpdateProjectMemberRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.ProjectMemberResponse;
import com.example.sprintsight.dtos.responses.ProjectResponse;
import com.example.sprintsight.dtos.responses.UserResponse;
import com.example.sprintsight.dtos.validation.ValidationGroups;
import com.example.sprintsight.entities.InvitationStatus;
import com.example.sprintsight.entities.ProjectMember;
import com.example.sprintsight.services.ProjectMemberService;
import com.example.sprintsight.services.ProjectService;
import com.example.sprintsight.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("#id == authentication.principal.id")
public class ProjectMemberController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ProjectMemberService projectMemberService;
    private final ProjectService projectService;
    private final UserService userService;

    @PostMapping("/sendInvitation/{projectId}/{senderId}")
    public ResponseEntity<ApiResponse<Object>> inviteProjectMember(@RequestBody SendInvitationRequest request,
                                                                   @PathVariable UUID projectId,@PathVariable UUID senderId){
        ProjectResponse project = projectService.getProject(projectId);
        Invitation invitation = new Invitation(
                request, projectId,senderId,project.name(),"you have been invited to this project", InvitationStatus.PENDING
        );
        UserResponse user = userService.getUser(request.userId());

        messagingTemplate.convertAndSendToUser(user.username(), "/queue/messages", invitation);

        return ResponseEntity.ok(new ApiResponse<>("invitation sent successfully", null));
    }

    @PostMapping("/invitationResponse")
    public void invitationRsponse(@RequestBody Invitation invitation){
        UserResponse user = userService.getUser(invitation.senderId());
        if(invitation.status() == InvitationStatus.ACCEPTED){
            projectMemberService.addProjectMember(invitation.projectMemberRequest(),invitation.projectId());
            messagingTemplate.convertAndSendToUser(user.username(), "/queue/messages", "your invitation has been accepted");
        }else if (invitation.status() == InvitationStatus.REJECTED){
            messagingTemplate.convertAndSendToUser(user.username(), "/queue/messages", "your invitation has been rejected");
        }
    }

    @PutMapping("/projectMember/{userId}/{projectId}")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> updateProjectMember(@Validated(ValidationGroups.Patch.class) @RequestBody UpdateProjectMemberRequest request,
                                                                                  @PathVariable UUID userId,
                                                                                  @PathVariable UUID projectId){
       ProjectMemberResponse projectMemberResponse = projectMemberService.updateProjectMember(request,userId,projectId);
        return ResponseEntity.ok(new ApiResponse<>("Project member updated successfully", projectMemberResponse));
    }

    @DeleteMapping("/projectMember/{userId}/{projectId}")
    public ResponseEntity<ApiResponse<Object>> deleteProjectMember(@PathVariable UUID userId,
                                                                   @PathVariable UUID projectId){
        projectMemberService.deleteProjectMember(userId, projectId);
        return ResponseEntity.ok(new ApiResponse<>("Project member deleted successfully", null));
    }

    @GetMapping("/projectMember/{projectId}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsersInProject(@PathVariable UUID projectId){
        List<UserResponse> allUsersInProject = List.of();

        Optional<List<ProjectMember>> projectMemberResponseList = projectMemberService.getAllProjectMembers(projectId);
        if (projectMemberResponseList.isPresent()){
            for (ProjectMember member : projectMemberResponseList.get()){
                allUsersInProject.add(userService.getUser(member.getId().getUserId()));
            }
            return ResponseEntity.ok(new ApiResponse<>("Project members retrieved successfully", allUsersInProject));
        }

         return ResponseEntity.ok(new ApiResponse<>("no members found", null));
    }

    @GetMapping("/projectMember/{userId}")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllUserProjects(UUID userId){
        List<ProjectResponse> allProjects = List.of();

        Optional<List<ProjectMember>> projectMemberResponseList = projectMemberService.getAllMemberProjects(userId);
        if (projectMemberResponseList.isPresent()){
            for (ProjectMember member : projectMemberResponseList.get()){
                allProjects.add(projectService.getProject(member.getId().getProjectId()));
            }
            return ResponseEntity.ok(new ApiResponse<>("Projects retrieved successfully", allProjects));
        }

        return ResponseEntity.ok(new ApiResponse<>("no projects found", null));
    }

}
