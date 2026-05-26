package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.InvitationResponse;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/invitations", produces = MediaType.APPLICATION_JSON_VALUE)
public class InvitationController {
    private final InvitationService invitationService;

    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<ApiResponse<InvitationResponse>> acceptInvitation(
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Invitation accepted",
                invitationService.acceptInvitation(invitationId, principal.getId())));
    }

    @PostMapping("/{invitationId}/reject")
    public ResponseEntity<ApiResponse<InvitationResponse>> rejectInvitation(
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Invitation rejected",
                invitationService.rejectInvitation(invitationId, principal.getId())));
    }
}
