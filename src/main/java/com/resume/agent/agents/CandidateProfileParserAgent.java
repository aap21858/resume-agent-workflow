package com.resume.agent.agents;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.agent.model.CandidateProfile;
import com.resume.agent.service.OpenAIClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Helper agent for parsing candidate profiles from resume text.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CandidateProfileParserAgent {
    
    private final OpenAIClientService openAIService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String SYSTEM_PROMPT = """
            You are an expert resume parser and HR analyst.
            Your task is to extract structured information from a resume.
            
            Extract:
            1. Name, email, phone
            2. Professional summary
            3. Technical skills
            4. Soft skills
            5. Work experience (company, position, duration, responsibilities)
            6. Education (institution, degree, field, graduation date)
            7. Certifications
            8. Projects
            
            Return your response as a valid JSON object with this structure:
            {
              "name": "John Doe",
              "email": "john@example.com",
              "phone": "+1234567890",
              "summary": "Professional summary...",
              "technicalSkills": ["Java", "Spring Boot", "AWS"],
              "softSkills": ["Leadership", "Communication"],
              "certifications": ["AWS Certified"],
              "projects": ["Project description 1"],
              "workExperience": [
                {
                  "company": "Company Name",
                  "position": "Software Engineer",
                  "duration": "2 years",
                  "startDate": "Jan 2020",
                  "endDate": "Jan 2022",
                  "responsibilities": ["resp1", "resp2"],
                  "achievements": ["achievement1"]
                }
              ],
              "education": [
                {
                  "institution": "University Name",
                  "degree": "Bachelor of Science",
                  "field": "Computer Science",
                  "graduationDate": "2020",
                  "gpa": "3.8"
                }
              ]
            }
            """;
    
    /**
     * Parse candidate profile from resume text.
     */
    public CandidateProfile parseProfile(String candidateId, String resumeText) {
        log.info("Parsing profile for candidate: {}", candidateId);
        
        try {
            String userPrompt = "Parse the following resume:\n\n" + resumeText;
            String response = openAIService.generateResponseWithRetry(SYSTEM_PROMPT, userPrompt, 3);
            
            // Extract JSON from response
            String jsonResponse = extractJson(response);
            
            // Parse the JSON response
            Map<String, Object> parsedData = objectMapper.readValue(
                jsonResponse,
                new TypeReference<Map<String, Object>>() {}
            );
            
            // Build CandidateProfile object
            CandidateProfile profile = CandidateProfile.builder()
                    .candidateId(candidateId)
                    .name((String) parsedData.getOrDefault("name", "Unknown"))
                    .email((String) parsedData.getOrDefault("email", ""))
                    .phone((String) parsedData.getOrDefault("phone", ""))
                    .createdAt(LocalDateTime.now())
                    .summary((String) parsedData.getOrDefault("summary", ""))
                    .technicalSkills(getListFromMap(parsedData, "technicalSkills"))
                    .softSkills(getListFromMap(parsedData, "softSkills"))
                    .certifications(getListFromMap(parsedData, "certifications"))
                    .projects(getListFromMap(parsedData, "projects"))
                    .workExperience(parseWorkExperience(parsedData))
                    .education(parseEducation(parsedData))
                    .build();
            
            log.info("Successfully parsed profile for: {}", profile.getName());
            return profile;
            
        } catch (Exception e) {
            log.error("Failed to parse candidate profile", e);
            throw new RuntimeException("Failed to parse candidate profile", e);
        }
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
    
    @SuppressWarnings("unchecked")
    private List<CandidateProfile.WorkExperience> parseWorkExperience(Map<String, Object> map) {
        Object value = map.get("workExperience");
        List<CandidateProfile.WorkExperience> experiences = new ArrayList<>();
        
        if (value instanceof List) {
            List<Map<String, Object>> expList = (List<Map<String, Object>>) value;
            for (Map<String, Object> expMap : expList) {
                CandidateProfile.WorkExperience exp = CandidateProfile.WorkExperience.builder()
                        .company((String) expMap.getOrDefault("company", ""))
                        .position((String) expMap.getOrDefault("position", ""))
                        .duration((String) expMap.getOrDefault("duration", ""))
                        .startDate((String) expMap.getOrDefault("startDate", ""))
                        .endDate((String) expMap.getOrDefault("endDate", ""))
                        .responsibilities(getListFromMap(expMap, "responsibilities"))
                        .achievements(getListFromMap(expMap, "achievements"))
                        .build();
                experiences.add(exp);
            }
        }
        
        return experiences;
    }
    
    @SuppressWarnings("unchecked")
    private List<CandidateProfile.Education> parseEducation(Map<String, Object> map) {
        Object value = map.get("education");
        List<CandidateProfile.Education> educationList = new ArrayList<>();
        
        if (value instanceof List) {
            List<Map<String, Object>> eduList = (List<Map<String, Object>>) value;
            for (Map<String, Object> eduMap : eduList) {
                CandidateProfile.Education edu = CandidateProfile.Education.builder()
                        .institution((String) eduMap.getOrDefault("institution", ""))
                        .degree((String) eduMap.getOrDefault("degree", ""))
                        .field((String) eduMap.getOrDefault("field", ""))
                        .graduationDate((String) eduMap.getOrDefault("graduationDate", ""))
                        .gpa((String) eduMap.getOrDefault("gpa", ""))
                        .build();
                educationList.add(edu);
            }
        }
        
        return educationList;
    }
}
