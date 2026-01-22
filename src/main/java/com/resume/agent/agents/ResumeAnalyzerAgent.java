package com.resume.agent.agents;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.agent.model.AnalysisResult;
import com.resume.agent.model.CandidateProfile;
import com.resume.agent.model.ClientRequirement;
import com.resume.agent.service.OpenAIClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Agent responsible for analyzing candidate fit against client requirements.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ResumeAnalyzerAgent {
    
    private final OpenAIClientService openAIService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String SYSTEM_PROMPT = """
            You are an expert technical recruiter and talent analyst.
            Your task is to analyze how well a candidate matches a job requirement.
            
            Analyze the following aspects:
            1. Technical skills match (0-100 scale)
            2. Experience level match
            3. Overall fit score (0-100 scale)
            4. Strengths of the candidate for this role
            5. Gaps or missing qualifications
            6. Specific matching and missing skills
            7. Recommendation for interview (yes/no)
            
            Be thorough and objective in your analysis.
            
            Return your response as a valid JSON object with this structure:
            {
              "fitScore": 85,
              "categoryScores": {
                "technical_skills": 90,
                "experience": 80,
                "education": 85,
                "overall_fit": 85
              },
              "strengths": ["strength1", "strength2"],
              "gaps": ["gap1", "gap2"],
              "matchingSkills": ["skill1", "skill2"],
              "missingSkills": ["skill1", "skill2"],
              "overallAssessment": "detailed assessment text",
              "recommendations": ["recommendation1", "recommendation2"],
              "recommendForInterview": true,
              "skillMatchPercentage": 85,
              "experienceMatchPercentage": 80
            }
            """;
    
    /**
     * Analyze candidate fit against client requirements.
     */
    public AnalysisResult analyzeCandidate(
            String candidateId,
            CandidateProfile candidateProfile,
            ClientRequirement clientRequirement) {
        
        log.info("Analyzing candidate {} for client {}", candidateId, clientRequirement.getClientId());
        
        try {
            String userPrompt = buildAnalysisPrompt(candidateProfile, clientRequirement);
            String response = openAIService.generateResponseWithRetry(SYSTEM_PROMPT, userPrompt, 3);
            
            // Extract JSON from response
            String jsonResponse = extractJson(response);
            
            // Parse the JSON response
            Map<String, Object> parsedData = objectMapper.readValue(
                jsonResponse,
                new TypeReference<Map<String, Object>>() {}
            );
            
            // Build AnalysisResult object
            AnalysisResult result = AnalysisResult.builder()
                    .analysisId(UUID.randomUUID().toString())
                    .candidateId(candidateId)
                    .clientId(clientRequirement.getClientId())
                    .analyzedAt(LocalDateTime.now())
                    .fitScore(getIntFromMap(parsedData, "fitScore", 0))
                    .categoryScores(getIntMapFromMap(parsedData, "categoryScores"))
                    .strengths(getListFromMap(parsedData, "strengths"))
                    .gaps(getListFromMap(parsedData, "gaps"))
                    .matchingSkills(getListFromMap(parsedData, "matchingSkills"))
                    .missingSkills(getListFromMap(parsedData, "missingSkills"))
                    .overallAssessment((String) parsedData.getOrDefault("overallAssessment", ""))
                    .recommendations(getListFromMap(parsedData, "recommendations"))
                    .recommendForInterview((Boolean) parsedData.getOrDefault("recommendForInterview", false))
                    .skillMatch(AnalysisResult.SkillMatch.builder()
                            .matchPercentage(getIntFromMap(parsedData, "skillMatchPercentage", 0))
                            .matchedSkills(getListFromMap(parsedData, "matchingSkills"))
                            .missingCriticalSkills(getListFromMap(parsedData, "missingSkills"))
                            .build())
                    .experienceMatch(AnalysisResult.ExperienceMatch.builder()
                            .matchPercentage(getIntFromMap(parsedData, "experienceMatchPercentage", 0))
                            .experienceLevel(candidateProfile.getWorkExperience() != null ? 
                                    "Experienced" : "Entry-level")
                            .build())
                    .build();
            
            log.info("Analysis complete - Fit Score: {}, Recommend: {}", 
                    result.getFitScore(), result.isRecommendForInterview());
            return result;
            
        } catch (Exception e) {
            log.error("Failed to analyze candidate", e);
            throw new RuntimeException("Failed to analyze candidate fit", e);
        }
    }
    
    private String buildAnalysisPrompt(CandidateProfile candidate, ClientRequirement requirement) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("=== JOB REQUIREMENT ===\n");
        prompt.append("Position: ").append(requirement.getJobTitle()).append("\n");
        prompt.append("Experience Level: ").append(requirement.getExperienceLevel()).append("\n");
        prompt.append("Required Skills: ").append(String.join(", ", requirement.getRequiredSkills())).append("\n");
        prompt.append("Preferred Skills: ").append(String.join(", ", requirement.getPreferredSkills())).append("\n");
        prompt.append("Technologies: ").append(String.join(", ", requirement.getTechnologies())).append("\n\n");
        
        prompt.append("=== CANDIDATE PROFILE ===\n");
        prompt.append("Name: ").append(candidate.getName()).append("\n");
        prompt.append("Technical Skills: ").append(String.join(", ", candidate.getTechnicalSkills())).append("\n");
        
        if (candidate.getWorkExperience() != null && !candidate.getWorkExperience().isEmpty()) {
            prompt.append("\nWork Experience:\n");
            for (CandidateProfile.WorkExperience exp : candidate.getWorkExperience()) {
                prompt.append("- ").append(exp.getPosition())
                      .append(" at ").append(exp.getCompany())
                      .append(" (").append(exp.getDuration()).append(")\n");
            }
        }
        
        if (candidate.getEducation() != null && !candidate.getEducation().isEmpty()) {
            prompt.append("\nEducation:\n");
            for (CandidateProfile.Education edu : candidate.getEducation()) {
                prompt.append("- ").append(edu.getDegree())
                      .append(" in ").append(edu.getField())
                      .append(" from ").append(edu.getInstitution()).append("\n");
            }
        }
        
        prompt.append("\n=== ANALYSIS REQUEST ===\n");
        prompt.append("Analyze this candidate's fit for the job requirement and provide a detailed assessment.");
        
        return prompt.toString();
    }
    
    private String extractJson(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
    
    @SuppressWarnings("unchecked")
    private List<String> getListFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return new ArrayList<>();
    }
    
    private int getIntFromMap(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Integer> getIntMapFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Map) {
            Map<String, Object> rawMap = (Map<String, Object>) value;
            Map<String, Integer> result = new HashMap<>();
            rawMap.forEach((k, v) -> {
                if (v instanceof Number) {
                    result.put(k, ((Number) v).intValue());
                }
            });
            return result;
        }
        return new HashMap<>();
    }
}
