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
public class InterviewPrep {
    private String id;
    private String candidateId;
    private String clientId;
    private List<String> technicalQuestions;
    private List<String> behavioralQuestions;
    private List<String> talkingPoints;
    private List<String> tips;
    private LocalDateTime generatedAt;
}
