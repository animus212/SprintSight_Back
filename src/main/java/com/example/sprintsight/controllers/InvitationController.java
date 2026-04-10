package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.SendInvitationRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.InvitationResponse;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/invitations", produces = MediaType.APPLICATION_JSON_VALUE)
public class InvitationController {
    private final InvitationService invitationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getPendingInvitations(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Invitations retrieved successfully",
                invitationService.getPendingInvitations(principal.getId())
        ));
    }

    @PostMapping("/projects/{projectId}")
    public ResponseEntity<ApiResponse<InvitationResponse>> sendInvitation(
            @PathVariable UUID projectId,
            @Valid @RequestBody SendInvitationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Invitation sent successfully",
                invitationService.sendInvitation(request, projectId, principal.getId())
        ));
    }

    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<ApiResponse<InvitationResponse>> acceptInvitation(
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Invitation accepted",
                invitationService.acceptInvitation(invitationId, principal.getId())
        ));
    }

    @PostMapping("/{invitationId}/reject")
    public ResponseEntity<ApiResponse<InvitationResponse>> rejectInvitation(
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Invitation rejected",
                invitationService.rejectInvitation(invitationId, principal.getId())
        ));
    }
}
