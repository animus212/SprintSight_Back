package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.CommentRequest;
import com.example.sprintsight.dtos.responses.CommentResponse;
import com.example.sprintsight.entities.*;
import com.example.sprintsight.mappers.CommentMapper;
import com.example.sprintsight.repositories.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final IssueService issueService;
    private final UserService userService;
    private final ProjectAuthorizationService authorizationService;
    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(UUID issueId, UUID principalId) {
        Issue issue = issueService.findIssue(issueId);

        authorizationService.getMemberOrThrow(principalId, issue.getProject().getId());

        return commentRepository.findByIssue_IdOrderByCreatedAtAsc(issueId)
                .stream()
                .map(commentMapper::toCommentResponse)
                .toList();
    }

    @Transactional
    public CommentResponse addComment(CommentRequest request, UUID issueId, UUID principalId) {
        Issue issue = issueService.findIssue(issueId);

        authorizationService.requireAnyRole(principalId, issue.getProject().getId(), ProjectRole.PRODUCT_OWNER,
                ProjectRole.SCRUM_MASTER, ProjectRole.DEVELOPER);

        User author = userService.findUser(principalId);

        Comment comment = commentMapper.toEntity(request);
        comment.setIssue(issue);
        comment.setAuthor(author);

        Comment saved = commentRepository.save(comment);

        log.info("Comment {} added to issue {}", saved.getId(), issueId);

        return commentMapper.toCommentResponse(saved);
    }

    @Transactional
    public CommentResponse updateComment(CommentRequest request, UUID commentId, UUID principalId) {
        Comment comment = findComment(commentId);

        verifyCommentOwnership(comment, principalId);

        comment.setContent(request.content());
        Comment saved = commentRepository.save(comment);

        return commentMapper.toCommentResponse(saved);
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID principalId) {
        Comment comment = findComment(commentId);

        verifyCommentOwnership(comment, principalId);

        commentRepository.delete(comment);

        log.info("Deleted comment {}", commentId);
    }

    private Comment findComment(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
    }

    private void verifyCommentOwnership(Comment comment, UUID principalId) {
        if (!comment.getAuthor().getId().equals(principalId)) {
            throw new AccessDeniedException("You can only modify your own comments");
        }
    }
}
