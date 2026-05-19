package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.IssueStatusConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssueStatusConfigurationRepository extends JpaRepository<IssueStatusConfiguration, UUID> {
    List<IssueStatusConfiguration> findByProject_IdOrderByOrderIndexAsc(UUID projectId);
    Optional<IssueStatusConfiguration> findByProject_IdAndIsDefaultTrue(UUID projectId);
    List<IssueStatusConfiguration> findByProject_IdAndIsCompletedTrue(UUID projectId);
    boolean existsByNameAndProject_Id(String name, UUID projectId);
    boolean existsByProject_IdAndIdNot(UUID projectId, UUID excludeId);
    boolean existsByProject_IdAndIsCompletedTrue(UUID projectId);
}
