package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.Sprint;
import com.example.sprintsight.entities.SprintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {
    List<Sprint> findByProject_Id(UUID projectId);
    Page<Sprint> findByProject_Id(UUID projectId, Pageable pageable);
    List<Sprint> findByProject_IdAndStatus(UUID projectId, SprintStatus status);
    boolean existsByProject_IdAndStatus(UUID projectId, SprintStatus status);
}
