package com.resume.agent.agents;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.agent.model.ClientRequirement;
import com.resume.agent.model.InterviewPrep;
import com.resume.agent.service.OpenAIClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Agent responsible for generating interview preparation materials.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InterviewPrepAgent {
    
    private final OpenAIClientService openAIService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String SYSTEM_PROMPT = """
            You are an expert interview coach and technical recruiter.
            Your task is to prepare comprehensive interview materials for a candidate.
            
            Generate:
            1. Technical questions (basic, intermediate, advanced levels)
            2. Behavioral questions aligned with the role
            3. Talking points and suggested answers
            4. Key topics to review
            5. Company research points
            
            For each question, provide:
            - The question text
            - Category (e.g., "Java", "Spring Boot", "System Design")
            - Difficulty level
            - A suggested answer or key points to cover
            
            Return your response as a valid JSON object with this structure:
            {
              "basicTechnicalQuestions": [
                {"question": "...", "category": "...", "difficulty": "basic", "suggestedAnswer": "...", "keyPoints": ["point1", "point2"]}
              ],
              "intermediateTechnicalQuestions": [...],
              "advancedTechnicalQuestions": [...],
              "behavioralQuestions": [...],
              "talkingPoints": [
                {"topic": "...", "talking": "...", "examples": ["example1", "example2"]}
              ],
              "keyTopicsToReview": ["topic1", "topic2"],
              "companyResearchPoints": ["point1", "point2"]
            }
            """;
    
    /**
     * Generate interview preparation materials.
     */
    public InterviewPrep generateInterviewPrep(
            String candidateId,
            ClientRequirement clientRequirement) {
        
        log.info("Generating interview prep for candidate {} and client {}", 
                candidateId, clientRequirement.getClientId());
        
        try {
            String userPrompt = buildPrepPrompt(clientRequirement);
            String response = openAIService.generateResponseWithRetry(SYSTEM_PROMPT, userPrompt, 3);
            
            // Extract JSON from response
            String jsonResponse = extractJson(response);
            
            // Parse the JSON response
            Map<String, Object> parsedData = objectMapper.readValue(
                jsonResponse,
                new TypeReference<Map<String, Object>>() {}
            );
            
            // Build InterviewPrep object
            InterviewPrep prep = InterviewPrep.builder()
                    .prepId(UUID.randomUUID().toString())
                    .candidateId(candidateId)
                    .clientId(clientRequirement.getClientId())
                    .createdAt(LocalDateTime.now())
                    .basicTechnicalQuestions(parseQuestions(parsedData, "basicTechnicalQuestions"))
                    .intermediateTechnicalQuestions(parseQuestions(parsedData, "intermediateTechnicalQuestions"))
                    .advancedTechnicalQuestions(parseQuestions(parsedData, "advancedTechnicalQuestions"))
                    .behavioralQuestions(parseQuestions(parsedData, "behavioralQuestions"))
                    .talkingPoints(parseTalkingPoints(parsedData, "talkingPoints"))
                    .keyTopicsToReview(getListFromMap(parsedData, "keyTopicsToReview"))
                    .companyResearchPoints(getListFromMap(parsedData, "companyResearchPoints"))
                    .build();
            
            int totalQuestions = prep.getBasicTechnicalQuestions().size() +
                                prep.getIntermediateTechnicalQuestions().size() +
                                prep.getAdvancedTechnicalQuestions().size() +
                                prep.getBehavioralQuestions().size();
            
            log.info("Generated interview prep with {} total questions", totalQuestions);
            return prep;
            
        } catch (Exception e) {
            log.error("Failed to generate interview prep", e);
            throw new RuntimeException("Failed to generate interview preparation materials", e);
        }
    }
    
    private String buildPrepPrompt(ClientRequirement requirement) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("=== JOB REQUIREMENT ===\n");
        prompt.append("Position: ").append(requirement.getJobTitle()).append("\n");
        prompt.append("Experience Level: ").append(requirement.getExperienceLevel()).append("\n");
        prompt.append("Required Skills: ").append(String.join(", ", requirement.getRequiredSkills())).append("\n");
        prompt.append("Technologies: ").append(String.join(", ", requirement.getTechnologies())).append("\n");
        
        if (requirement.getResponsibilities() != null && !requirement.getResponsibilities().isEmpty()) {
            prompt.append("Responsibilities: ")
                  .append(String.join(", ", requirement.getResponsibilities())).append("\n");
        }
        
        prompt.append("\n=== TASK ===\n");
        prompt.append("Generate comprehensive interview preparation materials for this position.\n");
        prompt.append("Focus on the required skills and technologies mentioned above.\n");
        prompt.append("Provide 5 questions for each difficulty level (basic, intermediate, advanced).\n");
        prompt.append("Include 5 behavioral questions.\n");
        prompt.append("Provide practical talking points and study topics.");
        
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
    private List<InterviewPrep.Question> parseQuestions(Map<String, Object> map, String key) {
        Object value = map.get(key);
        List<InterviewPrep.Question> questions = new ArrayList<>();
        
        if (value instanceof List) {
            List<Map<String, Object>> questionList = (List<Map<String, Object>>) value;
            for (Map<String, Object> qMap : questionList) {
                InterviewPrep.Question question = InterviewPrep.Question.builder()
                        .question((String) qMap.getOrDefault("question", ""))
                        .category((String) qMap.getOrDefault("category", "General"))
                        .difficulty((String) qMap.getOrDefault("difficulty", "intermediate"))
                        .suggestedAnswer((String) qMap.getOrDefault("suggestedAnswer", ""))
                        .keyPoints(getListFromMap(qMap, "keyPoints"))
                        .build();
                questions.add(question);
            }
        }
        
        return questions;
    }
    
    @SuppressWarnings("unchecked")
    private List<InterviewPrep.TalkingPoint> parseTalkingPoints(Map<String, Object> map, String key) {
        Object value = map.get(key);
        List<InterviewPrep.TalkingPoint> talkingPoints = new ArrayList<>();
        
        if (value instanceof List) {
            List<Map<String, Object>> pointList = (List<Map<String, Object>>) value;
            for (Map<String, Object> pMap : pointList) {
                InterviewPrep.TalkingPoint point = InterviewPrep.TalkingPoint.builder()
                        .topic((String) pMap.getOrDefault("topic", ""))
                        .talking((String) pMap.getOrDefault("talking", ""))
                        .examples(getListFromMap(pMap, "examples"))
                        .build();
                talkingPoints.add(point);
            }
        }
        
        return talkingPoints;
    }
    
    @SuppressWarnings("unchecked")
    private List<String> getListFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return new ArrayList<>();
    }
}
