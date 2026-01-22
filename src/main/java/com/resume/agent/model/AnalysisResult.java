package com.resume.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents the analysis result of a candidate against client requirements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    
    private String analysisId;
    private String candidateId;
    private String clientId;
    private LocalDateTime analyzedAt;
    
    // Scoring
    private int fitScore; // 0-100 scale
    private Map<String, Integer> categoryScores; // Category to score mapping
    
    // Detailed analysis
    private List<String> strengths;
    private List<String> gaps;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    
    // Recommendations
    private String overallAssessment;
    private List<String> recommendations;
    private boolean recommendForInterview;
    
    // Detailed breakdown
    private SkillMatch skillMatch;
    private ExperienceMatch experienceMatch;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillMatch {
        private int matchPercentage;
        private List<String> matchedSkills;
        private List<String> partiallyMatchedSkills;
        private List<String> missingCriticalSkills;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceMatch {
        private int matchPercentage;
        private String experienceLevel;
        private List<String> relevantExperience;
        private String experienceGap;
    }
}
