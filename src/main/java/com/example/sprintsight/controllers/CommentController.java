package com.example.sprintsight.controllers;

import com.example.sprintsight.dtos.requests.CommentRequest;
import com.example.sprintsight.dtos.responses.ApiResponse;
import com.example.sprintsight.dtos.responses.CommentResponse;
import com.example.sprintsight.security.UserPrincipal;
import com.example.sprintsight.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/issues/{issueId}/comments",
        produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable UUID issueId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(new ApiResponse<>("Comments retrieved successfully",
                commentService.getComments(issueId, principal.getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable UUID issueId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CommentResponse comment = commentService.addComment(request, issueId, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Comment added successfully", comment));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable UUID issueId,
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(new ApiResponse<>("Comment updated successfully",
                commentService.updateComment(request, commentId, principal.getId())));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable UUID issueId,
            @PathVariable UUID commentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        commentService.deleteComment(commentId, principal.getId());
        return ResponseEntity.ok(new ApiResponse<>("Comment deleted successfully", null));
    }
}
