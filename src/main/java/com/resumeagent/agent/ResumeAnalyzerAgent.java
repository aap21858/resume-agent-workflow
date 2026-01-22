package com.resumeagent.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeagent.model.AnalysisResult;
import com.resumeagent.model.ClientRequirement;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class ResumeAnalyzerAgent implements Agent {
    
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    
    public ResumeAnalyzerAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public String getAgentName() {
        return "Resume Analyzer Agent";
    }
    
    public AnalysisResult analyze(String candidateId, String resumeText, ClientRequirement requirement) {
        String promptText = """
            Given the candidate resume text and client requirements, analyze the fit.
            Return ONLY a valid JSON object (no markdown, no code blocks) with:
            {
              "fit_score": 75,
              "matched_skills": ["skill1", "skill2"],
              "missing_skills": ["skill3"],
              "skill_evidence": {"skill1": "evidence from resume"},
              "recommendations": ["recommendation1", "recommendation2"]
            }
            
            Client Requirements:
            Role: {role}
            Level: {level}
            Required Skills: {requiredSkills}
            Years of Experience: {years}
            
            Candidate Resume:
            {resumeText}
            """;
        
        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Map<String, Object> params = new HashMap<>();
        params.put("role", requirement.getRole());
        params.put("level", requirement.getLevel());
        params.put("requiredSkills", String.join(", ", requirement.getRequiredSkills()));
        params.put("years", requirement.getYearsOfExperience());
        params.put("resumeText", resumeText.substring(0, Math.min(resumeText.length(), 3000)));
        
        Prompt prompt = promptTemplate.create(params);
        String response = chatClient.prompt(prompt).call().content();
        
        return parseAnalysisResponse(response, candidateId, requirement.getId());
    }
    
    private AnalysisResult parseAnalysisResponse(String jsonResponse, String candidateId, String clientId) {
        try {
            // Clean up the response
            String cleanedJson = jsonResponse.trim();
            if (cleanedJson.startsWith("```json")) {
                cleanedJson = cleanedJson.substring(7);
            }
            if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.substring(3);
            }
            if (cleanedJson.endsWith("```")) {
                cleanedJson = cleanedJson.substring(0, cleanedJson.length() - 3);
            }
            cleanedJson = cleanedJson.trim();
            
            JsonNode jsonNode = objectMapper.readTree(cleanedJson);
            
            List<String> matchedSkills = new ArrayList<>();
            if (jsonNode.has("matched_skills")) {
                jsonNode.get("matched_skills").forEach(node -> matchedSkills.add(node.asText()));
            }
            
            List<String> missingSkills = new ArrayList<>();
            if (jsonNode.has("missing_skills")) {
                jsonNode.get("missing_skills").forEach(node -> missingSkills.add(node.asText()));
            }
            
            Map<String, String> skillEvidence = new HashMap<>();
            if (jsonNode.has("skill_evidence")) {
                JsonNode evidenceNode = jsonNode.get("skill_evidence");
                evidenceNode.fields().forEachRemaining(entry -> 
                    skillEvidence.put(entry.getKey(), entry.getValue().asText())
                );
            }
            
            List<String> recommendations = new ArrayList<>();
            if (jsonNode.has("recommendations")) {
                jsonNode.get("recommendations").forEach(node -> recommendations.add(node.asText()));
            }
            
            return AnalysisResult.builder()
                    .id(UUID.randomUUID().toString())
                    .candidateId(candidateId)
                    .clientId(clientId)
                    .fitScore(jsonNode.has("fit_score") ? jsonNode.get("fit_score").asInt() : 50)
                    .matchedSkills(matchedSkills)
                    .missingSkills(missingSkills)
                    .skillEvidence(skillEvidence)
                    .recommendations(recommendations)
                    .analyzedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            // Fallback
            return AnalysisResult.builder()
                    .id(UUID.randomUUID().toString())
                    .candidateId(candidateId)
                    .clientId(clientId)
                    .fitScore(50)
                    .matchedSkills(new ArrayList<>())
                    .missingSkills(new ArrayList<>())
                    .skillEvidence(new HashMap<>())
                    .recommendations(Arrays.asList("Unable to parse analysis results"))
                    .analyzedAt(LocalDateTime.now())
                    .build();
        }
    }
}
