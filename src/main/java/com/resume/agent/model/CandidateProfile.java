package com.resume.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a candidate's profile extracted from their resume.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfile {
    
    private String candidateId;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    
    // Experience and education
    private List<WorkExperience> workExperience;
    private List<Education> education;
    
    // Skills and competencies
    private List<String> technicalSkills;
    private List<String> softSkills;
    private List<String> certifications;
    
    // Additional info
    private String summary;
    private List<String> projects;
    private Map<String, Object> additionalInfo;
    
    // Original resume path
    private String originalResumePath;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkExperience {
        private String company;
        private String position;
        private String duration;
        private String startDate;
        private String endDate;
        private List<String> responsibilities;
        private List<String> achievements;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Education {
        private String institution;
        private String degree;
        private String field;
        private String graduationDate;
        private String gpa;
    }
}
