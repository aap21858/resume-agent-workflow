package com.resume.agent.agents;

import com.resume.agent.model.CandidateProfile;
import com.resume.agent.model.ClientRequirement;
import com.resume.agent.service.OpenAIClientService;
import com.resume.agent.service.PDFProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Agent responsible for modifying/tailoring resumes to match client requirements.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ResumeModifierAgent {
    
    private final OpenAIClientService openAIService;
    private final PDFProcessingService pdfProcessingService;
    
    private static final String SYSTEM_PROMPT = """
            You are an expert resume writer and career consultant.
            Your task is to tailor a candidate's resume to better match a specific job requirement.
            
            IMPORTANT RULES:
            1. NEVER fabricate or add false information
            2. Only reorganize, rephrase, and emphasize existing information
            3. Highlight relevant skills and experience that match the job requirements
            4. Reorder sections to put the most relevant information first
            5. Adjust terminology to match the client's language
            6. Keep the resume truthful and authentic
            7. Maintain professional formatting and structure
            
            Provide a complete, well-formatted resume as plain text that can be converted to PDF.
            Include standard sections: Summary, Skills, Experience, Education.
            """;
    
    /**
     * Generate a modified/tailored resume for a candidate based on client requirements.
     */
    public String generateModifiedResume(
            CandidateProfile candidateProfile,
            ClientRequirement clientRequirement,
            String originalResumeText) {
        
        log.info("Generating modified resume for candidate {} targeting client {}", 
                candidateProfile.getCandidateId(), clientRequirement.getClientId());
        
        try {
            String userPrompt = buildModificationPrompt(
                    candidateProfile, 
                    clientRequirement, 
                    originalResumeText
            );
            
            String modifiedResumeText = openAIService.generateResponseWithRetry(
                    SYSTEM_PROMPT, 
                    userPrompt, 
                    3
            );
            
            log.info("Successfully generated modified resume with {} characters", 
                    modifiedResumeText.length());
            return modifiedResumeText;
            
        } catch (Exception e) {
            log.error("Failed to generate modified resume", e);
            throw new RuntimeException("Failed to generate modified resume", e);
        }
    }
    
    /**
     * Generate modified resume and save as PDF.
     */
    public void generateModifiedResumePDF(
            CandidateProfile candidateProfile,
            ClientRequirement clientRequirement,
            String originalResumeText,
            String outputPath) {
        
        try {
            String modifiedText = generateModifiedResume(
                    candidateProfile, 
                    clientRequirement, 
                    originalResumeText
            );
            
            pdfProcessingService.generatePDF(modifiedText, outputPath);
            log.info("Saved modified resume PDF to: {}", outputPath);
            
        } catch (Exception e) {
            log.error("Failed to generate modified resume PDF", e);
            throw new RuntimeException("Failed to generate modified resume PDF", e);
        }
    }
    
    private String buildModificationPrompt(
            CandidateProfile candidate,
            ClientRequirement requirement,
            String originalResume) {
        
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("=== JOB REQUIREMENT ===\n");
        prompt.append("Position: ").append(requirement.getJobTitle()).append("\n");
        prompt.append("Experience Level: ").append(requirement.getExperienceLevel()).append("\n");
        prompt.append("Required Skills: ").append(String.join(", ", requirement.getRequiredSkills())).append("\n");
        prompt.append("Preferred Skills: ").append(String.join(", ", requirement.getPreferredSkills())).append("\n");
        prompt.append("Technologies: ").append(String.join(", ", requirement.getTechnologies())).append("\n");
        
        if (requirement.getResponsibilities() != null && !requirement.getResponsibilities().isEmpty()) {
            prompt.append("Key Responsibilities: ")
                  .append(String.join(", ", requirement.getResponsibilities())).append("\n");
        }
        
        prompt.append("\n=== ORIGINAL RESUME ===\n");
        prompt.append(originalResume).append("\n");
        
        prompt.append("\n=== TASK ===\n");
        prompt.append("Tailor this resume to highlight the candidate's qualifications for the ")
              .append(requirement.getJobTitle()).append(" position.\n");
        prompt.append("Focus on:\n");
        prompt.append("1. Emphasizing skills that match: ")
              .append(String.join(", ", requirement.getRequiredSkills())).append("\n");
        prompt.append("2. Using terminology from the job requirement\n");
        prompt.append("3. Highlighting relevant experience and projects\n");
        prompt.append("4. Creating a strong professional summary targeting this role\n");
        prompt.append("\nProvide the complete tailored resume as clean, well-formatted text.");
        
        return prompt.toString();
    }
}
