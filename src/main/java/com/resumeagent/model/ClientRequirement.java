package com.resumeagent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequirement {
    private String id;
    private String role;
    private String level; // junior, intermediate, senior
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private Integer yearsOfExperience;
    private String rawRequirement;
    private LocalDateTime createdAt;
}
