package com.resume.agent.orchestrator;

import com.resume.agent.agents.*;
import com.resume.agent.config.WorkflowConfig;
import com.resume.agent.dto.WorkflowRequest;
import com.resume.agent.dto.WorkflowResult;
import com.resume.agent.model.*;
import com.resume.agent.service.FileStorageService;
import com.resume.agent.service.PDFProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Orchestrator that coordinates the complete workflow across all agents.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowOrchestrator {
    
    private final ClientRequirementsParserAgent requirementsParserAgent;
    private final CandidateProfileParserAgent profileParserAgent;
    private final ResumeAnalyzerAgent analyzerAgent;
    private final ResumeModifierAgent modifierAgent;
    private final InterviewPrepAgent interviewPrepAgent;
    
    private final PDFProcessingService pdfProcessingService;
    private final FileStorageService fileStorageService;
    private final WorkflowConfig workflowConfig;
    
    /**
     * Execute the complete workflow.
     */
    public WorkflowResult execute(WorkflowRequest request) {
        log.info("Starting workflow execution");
        long startTime = System.currentTimeMillis();
        
        String workflowId = UUID.randomUUID().toString();
        String candidateId = request.getCandidateId() != null ? 
                request.getCandidateId() : UUID.randomUUID().toString();
        String clientId = request.getClientId() != null ? 
                request.getClientId() : UUID.randomUUID().toString();
        
        try {
            // Step 1: Parse client requirements
            log.info("Step 1: Parsing client requirements");
            ClientRequirement clientRequirement = requirementsParserAgent.parseRequirements(
                    clientId, 
                    request.getClientRequirement()
            );
            saveClientRequirement(clientId, clientRequirement);
            
            // Step 2: Extract text from resume PDF
            log.info("Step 2: Extracting resume text");
            String resumeText = pdfProcessingService.extractTextFromPDF(request.getCandidateResumePath());
            
            // Step 3: Parse candidate profile from resume
            log.info("Step 3: Parsing candidate profile");
            CandidateProfile candidateProfile = profileParserAgent.parseProfile(candidateId, resumeText);
            candidateProfile.setOriginalResumePath(request.getCandidateResumePath());
            saveCandidateProfile(candidateId, candidateProfile);
            
            // Step 4: Analyze candidate fit
            log.info("Step 4: Analyzing candidate fit");
            AnalysisResult analysisResult = analyzerAgent.analyzeCandidate(
                    candidateId, 
                    candidateProfile, 
                    clientRequirement
            );
            saveAnalysisResult(candidateId, clientId, analysisResult);
            
            // Step 5: Conditional workflow based on fit score
            String modifiedResumePath = null;
            InterviewPrep interviewPrep = null;
            
            int threshold = workflowConfig.getWorkflow().getFitScoreThreshold();
            if (analysisResult.getFitScore() >= threshold) {
                log.info("Fit score ({}) meets threshold ({}), proceeding with resume modification and interview prep", 
                        analysisResult.getFitScore(), threshold);
                
                // Step 6: Generate modified resume
                if (request.isGenerateModifiedResume()) {
                    log.info("Step 6: Generating modified resume");
                    modifiedResumePath = generateAndSaveModifiedResume(
                            candidateId, 
                            clientId, 
                            candidateProfile, 
                            clientRequirement, 
                            resumeText
                    );
                }
                
                // Step 7: Generate interview prep materials
                if (request.isGenerateInterviewPrep()) {
                    log.info("Step 7: Generating interview prep");
                    interviewPrep = interviewPrepAgent.generateInterviewPrep(candidateId, clientRequirement);
                    saveInterviewPrep(candidateId, clientId, interviewPrep);
                }
            } else {
                log.info("Fit score ({}) below threshold ({}), skipping resume modification and interview prep", 
                        analysisResult.getFitScore(), threshold);
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Workflow completed successfully in {} ms", executionTime);
            
            return WorkflowResult.builder()
                    .workflowId(workflowId)
                    .success(true)
                    .message("Workflow completed successfully")
                    .clientRequirement(clientRequirement)
                    .candidateProfile(candidateProfile)
                    .analysisResult(analysisResult)
                    .modifiedResumePath(modifiedResumePath)
                    .interviewPrep(interviewPrep)
                    .executionTimeMs(executionTime)
                    .status("COMPLETED")
                    .build();
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Workflow execution failed", e);
            
            return WorkflowResult.builder()
                    .workflowId(workflowId)
                    .success(false)
                    .message("Workflow failed: " + e.getMessage())
                    .executionTimeMs(executionTime)
                    .status("FAILED")
                    .build();
        }
    }
    
    private void saveClientRequirement(String clientId, ClientRequirement requirement) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filename = clientId + "-" + timestamp + ".json";
        fileStorageService.saveAsJson(requirement, "requirements", filename);
    }
    
    private void saveCandidateProfile(String candidateId, CandidateProfile profile) throws IOException {
        String filename = candidateId + "-profile.json";
        fileStorageService.saveAsJson(profile, "candidates", filename);
    }
    
    private void saveAnalysisResult(String candidateId, String clientId, AnalysisResult result) throws IOException {
        String filename = candidateId + "-" + clientId + "-analysis.json";
        fileStorageService.saveAsJson(result, "analysis", filename);
    }
    
    private String generateAndSaveModifiedResume(
            String candidateId,
            String clientId,
            CandidateProfile profile,
            ClientRequirement requirement,
            String resumeText) throws IOException {
        
        String filename = candidateId + "-" + clientId + ".pdf";
        String outputPath = fileStorageService.getFilePath("resumes/modified", filename);
        
        modifierAgent.generateModifiedResumePDF(profile, requirement, resumeText, outputPath);
        return outputPath;
    }
    
    private void saveInterviewPrep(String candidateId, String clientId, InterviewPrep prep) throws IOException {
        String filename = candidateId + "-" + clientId + "-prep.json";
        fileStorageService.saveAsJson(prep, "interview-prep", filename);
    }
}
