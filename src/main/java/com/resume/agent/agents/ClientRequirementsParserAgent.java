package com.resume.agent.agents;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.agent.model.ClientRequirement;
import com.resume.agent.service.OpenAIClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Agent responsible for parsing and structuring client requirements.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ClientRequirementsParserAgent {
    
    private final OpenAIClientService openAIService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String SYSTEM_PROMPT = """
            You are an expert HR analyst specializing in parsing job requirements.
            Your task is to extract and structure information from job descriptions.
            
            Extract the following information:
            1. Job title
            2. Experience level (entry, junior, intermediate, senior, expert)
            3. Required skills (must-have)
            4. Preferred skills (nice-to-have)
            5. Technologies and tools mentioned
            6. Key responsibilities
            7. Qualifications
            
            Also create a scoring rubric where each skill is weighted based on importance.
            Required skills should have higher weights (7-10).
            Preferred skills should have moderate weights (4-6).
            
            Return your response as a valid JSON object with the following structure:
            {
              "jobTitle": "string",
              "experienceLevel": "string",
              "requiredSkills": ["skill1", "skill2"],
              "preferredSkills": ["skill1", "skill2"],
              "technologies": ["tech1", "tech2"],
              "responsibilities": ["resp1", "resp2"],
              "qualifications": ["qual1", "qual2"],
              "skillWeights": {"skill1": 10, "skill2": 8},
              "scoringCriteria": {"technical_skills": "40%", "experience": "30%", "education": "15%", "other": "15%"}
            }
            """;
    
    /**
     * Parse client requirements from raw text.
     */
    public ClientRequirement parseRequirements(String clientId, String rawRequirement) {
        log.info("Parsing requirements for client: {}", clientId);
        
        try {
            String userPrompt = "Parse the following job requirement:\n\n" + rawRequirement;
            String response = openAIService.generateResponseWithRetry(SYSTEM_PROMPT, userPrompt, 3);
            
            // Extract JSON from response (handle markdown code blocks)
            String jsonResponse = extractJson(response);
            
            // Parse the JSON response
            Map<String, Object> parsedData = objectMapper.readValue(
                jsonResponse, 
                new TypeReference<Map<String, Object>>() {}
            );
            
            // Build ClientRequirement object
            ClientRequirement requirement = ClientRequirement.builder()
                    .clientId(clientId)
                    .jobTitle((String) parsedData.getOrDefault("jobTitle", "Unknown Position"))
                    .rawRequirement(rawRequirement)
                    .createdAt(LocalDateTime.now())
                    .experienceLevel((String) parsedData.getOrDefault("experienceLevel", "intermediate"))
                    .requiredSkills(getListFromMap(parsedData, "requiredSkills"))
                    .preferredSkills(getListFromMap(parsedData, "preferredSkills"))
                    .technologies(getListFromMap(parsedData, "technologies"))
                    .responsibilities(getListFromMap(parsedData, "responsibilities"))
                    .qualifications(getListFromMap(parsedData, "qualifications"))
                    .skillWeights(getMapFromMap(parsedData, "skillWeights"))
                    .scoringCriteria(getStringMapFromMap(parsedData, "scoringCriteria"))
                    .build();
            
            log.info("Successfully parsed requirements with {} required skills", 
                    requirement.getRequiredSkills().size());
            return requirement;
            
        } catch (Exception e) {
            log.error("Failed to parse requirements", e);
            throw new RuntimeException("Failed to parse client requirements", e);
        }
    }
    
    private String extractJson(String response) {
        // Remove markdown code blocks if present
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
    
    @SuppressWarnings("unchecked")
    private Map<String, Integer> getMapFromMap(Map<String, Object> map, String key) {
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
    
    @SuppressWarnings("unchecked")
    private Map<String, String> getStringMapFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Map) {
            return (Map<String, String>) value;
        }
        return new HashMap<>();
    }
}
