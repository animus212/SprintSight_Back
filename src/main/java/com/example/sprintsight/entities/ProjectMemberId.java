package com.example.sprintsight.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Embeddable
@AllArgsConstructor
public class ProjectMemberId {

    private UUID projectId;
    private UUID userId;
}
