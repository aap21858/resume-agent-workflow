package com.resumeagent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private String id;
    private String candidateId;
    private String clientId;
    private Integer fitScore; // 0-100
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private Map<String, String> skillEvidence; // skill -> evidence from resume
    private List<String> recommendations;
    private LocalDateTime analyzedAt;
}
