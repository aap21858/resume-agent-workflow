package com.resumeagent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {
    private String id;
    private String name;
    private String email;
    private List<String> skills;
    private Integer yearsOfExperience;
    private String originalResumeFilename;
    private LocalDateTime uploadedAt;
}
