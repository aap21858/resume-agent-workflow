package com.resume.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents interview preparation materials for a candidate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewPrep {
    
    private String prepId;
    private String candidateId;
    private String clientId;
    private LocalDateTime createdAt;
    
    // Technical questions
    private List<Question> basicTechnicalQuestions;
    private List<Question> intermediateTechnicalQuestions;
    private List<Question> advancedTechnicalQuestions;
    
    // Behavioral questions
    private List<Question> behavioralQuestions;
    
    // Talking points
    private List<TalkingPoint> talkingPoints;
    
    // Additional prep
    private List<String> keyTopicsToReview;
    private List<String> companyResearchPoints;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Question {
        private String question;
        private String category;
        private String difficulty;
        private String suggestedAnswer;
        private List<String> keyPoints;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TalkingPoint {
        private String topic;
        private String talking;
        private List<String> examples;
    }
}
