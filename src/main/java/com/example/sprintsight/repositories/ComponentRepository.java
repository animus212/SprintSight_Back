package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.Component;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComponentRepository extends JpaRepository<Component, UUID> {
    List<Component> findByProject_Id(UUID projectId);
    boolean existsByNameAndProject_Id(String name, UUID projectId);
}
