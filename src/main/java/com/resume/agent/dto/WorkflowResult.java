package com.resume.agent.dto;

import com.resume.agent.model.AnalysisResult;
import com.resume.agent.model.CandidateProfile;
import com.resume.agent.model.ClientRequirement;
import com.resume.agent.model.InterviewPrep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result DTO for workflow execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResult {
    
    private String workflowId;
    private boolean success;
    private String message;
    
    // Workflow outputs
    private ClientRequirement clientRequirement;
    private CandidateProfile candidateProfile;
    private AnalysisResult analysisResult;
    private String modifiedResumePath;
    private InterviewPrep interviewPrep;
    
    // Metadata
    private long executionTimeMs;
    private String status;
}
