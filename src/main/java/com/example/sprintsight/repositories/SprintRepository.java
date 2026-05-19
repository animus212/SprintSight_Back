package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.Sprint;
import com.example.sprintsight.entities.SprintStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {
    List<Sprint> findByProject_Id(UUID projectId);
    List<Sprint> findByProject_IdAndStatus(UUID projectId, SprintStatus status);
    Optional<Sprint> findByProject_IdAndStatus(UUID projectId, SprintStatus status, Pageable pageable);
    boolean existsByProject_IdAndStatus(UUID projectId, SprintStatus status);
}
