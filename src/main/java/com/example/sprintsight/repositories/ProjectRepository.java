package com.example.sprintsight.repositories;

import com.example.sprintsight.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {}
