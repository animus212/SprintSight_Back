package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.IssueTypeConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssueTypeConfigurationRepository extends JpaRepository<IssueTypeConfiguration, UUID> {
    List<IssueTypeConfiguration> findByProject_Id(UUID projectId);
    Optional<IssueTypeConfiguration> findByProject_IdAndIsDefaultTrue(UUID projectId);
    boolean existsByNameAndProject_Id(String name, UUID projectId);
    long countByProject_Id(UUID projectId);
}
