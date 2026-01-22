package com.resumeagent.agent;

import com.resumeagent.model.ClientRequirement;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class RequirementsParserAgent implements Agent {
    
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    
    public RequirementsParserAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public String getAgentName() {
        return "Requirements Parser Agent";
    }
    
    public ClientRequirement parse(String rawRequirement) {
        String promptText = """
            Analyze the following client requirement and extract structured information.
            Return ONLY a valid JSON object with the following structure (no markdown, no code blocks):
            {
              "role": "job title",
              "level": "junior/intermediate/senior",
              "required_skills": ["skill1", "skill2"],
              "preferred_skills": ["skill3", "skill4"],
              "years_of_experience": 3
            }
            
            Client Requirement:
            {requirement}
            """;
        
        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Map<String, Object> params = new HashMap<>();
        params.put("requirement", rawRequirement);
        Prompt prompt = promptTemplate.create(params);
        
        String response = chatClient.prompt(prompt).call().content();
        
        // Parse the JSON response
        return parseJsonResponse(response, rawRequirement);
    }
    
    private ClientRequirement parseJsonResponse(String jsonResponse, String rawRequirement) {
        try {
            // Clean up the response to extract JSON
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
            
            List<String> requiredSkills = new ArrayList<>();
            if (jsonNode.has("required_skills")) {
                jsonNode.get("required_skills").forEach(node -> requiredSkills.add(node.asText()));
            }
            
            List<String> preferredSkills = new ArrayList<>();
            if (jsonNode.has("preferred_skills")) {
                jsonNode.get("preferred_skills").forEach(node -> preferredSkills.add(node.asText()));
            }
            
            return ClientRequirement.builder()
                    .id(UUID.randomUUID().toString())
                    .role(jsonNode.has("role") ? jsonNode.get("role").asText() : "")
                    .level(jsonNode.has("level") ? jsonNode.get("level").asText() : "intermediate")
                    .requiredSkills(requiredSkills)
                    .preferredSkills(preferredSkills)
                    .yearsOfExperience(jsonNode.has("years_of_experience") ? jsonNode.get("years_of_experience").asInt() : 0)
                    .rawRequirement(rawRequirement)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            // Fallback: create a basic requirement
            return ClientRequirement.builder()
                    .id(UUID.randomUUID().toString())
                    .role("Developer")
                    .level("intermediate")
                    .requiredSkills(new ArrayList<>())
                    .preferredSkills(new ArrayList<>())
                    .yearsOfExperience(0)
                    .rawRequirement(rawRequirement)
                    .createdAt(LocalDateTime.now())
                    .build();
        }
    }
}
