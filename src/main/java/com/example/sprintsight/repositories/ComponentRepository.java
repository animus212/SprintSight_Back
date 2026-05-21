package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.Component;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ComponentRepository extends JpaRepository<Component, UUID> {
    List<Component> findByProject_Id(UUID projectId);
    Page<Component> findByProject_Id(UUID projectId, Pageable pageable);
    boolean existsByNameAndProject_Id(String name, UUID projectId);
    Set<Component> findAllByIdInAndProject_Id(Set<UUID> ids, UUID projectId);

    @Query("""
        SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
        FROM Issue i JOIN i.components c
        WHERE c.id = :componentId
    """)
    boolean isReferencedByAnyIssue(@Param("componentId") UUID componentId);
}
