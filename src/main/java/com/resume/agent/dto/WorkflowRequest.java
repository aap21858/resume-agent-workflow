package com.resume.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for workflow execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRequest {
    
    @NotBlank(message = "Client requirement is required")
    private String clientRequirement;
    
    private String clientId;
    
    @NotBlank(message = "Candidate resume path is required")
    private String candidateResumePath;
    
    private String candidateId;
    
    // Optional parameters
    private boolean generateModifiedResume = true;
    private boolean generateInterviewPrep = true;
}
