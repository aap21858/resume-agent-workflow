package com.resume.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents parsed and structured client requirements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequirement {
    
    private String clientId;
    private String jobTitle;
    private String rawRequirement;
    private LocalDateTime createdAt;
    
    // Structured requirements
    private String experienceLevel; // e.g., "intermediate", "senior"
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private List<String> technologies;
    private Map<String, Integer> skillWeights; // Skill to weight mapping for scoring
    
    // Additional details
    private String jobDescription;
    private String companyInfo;
    private List<String> responsibilities;
    private List<String> qualifications;
    
    // Scoring rubric
    private Map<String, String> scoringCriteria;
}
