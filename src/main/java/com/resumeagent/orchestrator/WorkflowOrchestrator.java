package com.resumeagent.orchestrator;

import com.resumeagent.model.AnalysisResult;
import com.resumeagent.model.InterviewPrep;
import com.resumeagent.service.AnalysisService;
import com.resumeagent.service.InterviewPrepService;
import com.resumeagent.service.ResumeService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WorkflowOrchestrator {
    
    private final AnalysisService analysisService;
    private final ResumeService resumeService;
    private final InterviewPrepService interviewPrepService;
    
    public WorkflowOrchestrator(AnalysisService analysisService,
                               ResumeService resumeService,
                               InterviewPrepService interviewPrepService) {
        this.analysisService = analysisService;
        this.resumeService = resumeService;
        this.interviewPrepService = interviewPrepService;
    }
    
    /**
     * Process a candidate for a specific client requirement
     * This coordinates the full workflow
     */
    public WorkflowResult processCandidate(String candidateId, String clientId) throws IOException {
        // 1. Analyze candidate fit
        AnalysisResult analysis = analysisService.analyzeCandidate(candidateId, clientId);
        
        String modifiedResumePath = null;
        
        // 2. If fit score > threshold, modify resume
        if (analysis.getFitScore() > 60) {
            modifiedResumePath = resumeService.optimizeResume(candidateId, clientId);
        }
        
        // 3. Generate interview prep
        InterviewPrep prep = interviewPrepService.generateInterviewPrep(candidateId, clientId);
        
        return new WorkflowResult(analysis, modifiedResumePath, prep);
    }
    
    @Data
    @AllArgsConstructor
    public static class WorkflowResult {
        private AnalysisResult analysis;
        private String modifiedResumePath;
        private InterviewPrep interviewPrep;
    }
}
