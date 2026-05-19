package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.IssuePriorityConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssuePriorityConfigurationRepository extends JpaRepository<IssuePriorityConfiguration, UUID> {
    List<IssuePriorityConfiguration> findByProject_IdOrderByOrderIndexAsc(UUID projectId);
    Optional<IssuePriorityConfiguration> findByProject_IdAndIsDefaultTrue(UUID projectId);
    boolean existsByNameAndProject_Id(String name, UUID projectId);
    boolean existsByProject_IdAndIdNot(UUID projectId, UUID excludeId);
}
