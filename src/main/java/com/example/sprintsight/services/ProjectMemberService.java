package com.example.sprintsight.services;

import com.example.sprintsight.dtos.requests.SendInvitationRequest;
import com.example.sprintsight.dtos.requests.UpdateProjectMemberRequest;
import com.example.sprintsight.dtos.responses.ProjectMemberResponse;
import com.example.sprintsight.entities.ProjectMember;
import com.example.sprintsight.entities.ProjectMemberId;
import com.example.sprintsight.exceptions.EntityNotFoundException;
import com.example.sprintsight.mappers.ProjectMemberMapper;
import com.example.sprintsight.repositories.ProjectMemberRepository;
import com.example.sprintsight.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMemberMapper projectMemberMapper;
    @Transactional
    public ProjectMemberResponse addProjectMember(SendInvitationRequest request, UUID projectId) {
        UserPrincipal principal = (UserPrincipal) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()
        ).getPrincipal();

        assert principal != null;
        boolean userExists = projectMemberRepository.existsByUserId(principal.getId());
        if (!userExists){
            throw new EntityNotFoundException("user not found");
        }

        ProjectMember projectMember = projectMemberMapper.toEntity(request);
        

        return saveProjectMember(projectMember, "Create project");
    }
    @Transactional
    public ProjectMemberResponse updateProjectMember(UpdateProjectMemberRequest request, UUID userId,UUID projectId) {
        Optional<ProjectMember> projectMember = findProjectMember(userId, projectId);

        projectMemberMapper.updateProjectMemberFromPut(request, projectMember.get());
        return saveProjectMember(projectMember.get(), "Updated projectMember");
    }

    private Optional<ProjectMember> findProjectMember(UUID userId, UUID projectId) {
        ProjectMemberId id= new ProjectMemberId(userId, projectId);
        return projectMemberRepository.findById(id);
    }

    @Transactional
    public void deleteProjectMember(UUID userId, UUID projectId) {
        Optional<ProjectMember> projectMember = findProjectMember(userId, projectId);

        projectMemberRepository.delete(projectMember.get());

        log.info("Deleted project member: {},{}", userId,projectId);
    }

    private ProjectMemberResponse saveProjectMember(ProjectMember projectMember, String logMessage) {
        ProjectMember savedProject = projectMemberRepository.save(projectMember);

        log.info("{}: {}", logMessage, savedProject.getId());


        return projectMemberMapper.toProjectMemberResponse(savedProject);
    }

    public Optional<List<ProjectMember>> getAllProjectMembers(UUID projectId){
        return projectMemberRepository.findByProjectId(projectId);

    }

    public Optional<List<ProjectMember>> getAllMemberProjects(UUID userId) {
        return projectMemberRepository.findByProjectId(userId);
    }
}
