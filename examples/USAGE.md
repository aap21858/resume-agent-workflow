# Example Usage

## Testing the Application

### 1. Start the Application

First, set your OpenAI API key:
```bash
export OPENAI_API_KEY=your-api-key-here
```

Then start the application:
```bash
mvn spring-boot:run
```

Or run the packaged JAR:
```bash
java -jar target/resume-agent-workflow-1.0.0.jar
```

### 2. Access the Web UI

Open your browser and navigate to: `http://localhost:8080`

### 3. Create a Client Requirement

**Using the Web UI:**
1. Click on "Clients" in the navigation
2. Enter a requirement in the text area, for example:
   ```
   Looking for a senior Java developer with 5+ years experience in Java 17, 
   Spring Boot 3.x, microservices architecture, AWS cloud services (EC2, S3, Lambda), 
   Docker, Kubernetes, and experience with RESTful API design.
   ```
3. Click "Create Requirement"
4. The AI will parse and structure the requirement

**Using the API:**
```bash
curl -X POST http://localhost:8080/api/clients \
  -H "Content-Type: application/json" \
  -d '{"requirement": "Looking for an intermediate Java developer with Java 17, Spring Boot, and AWS experience"}'
```

### 4. Upload a Candidate Resume

**Using the Web UI:**
1. Click on "Upload Resume" in the navigation
2. Fill in:
   - Candidate Name: John Doe
   - Email: john.doe@example.com
   - Select a PDF resume file
3. Click "Upload Resume"

**Using the API:**
```bash
curl -X POST http://localhost:8080/api/candidates/upload \
  -F "name=John Doe" \
  -F "email=john.doe@example.com" \
  -F "file=@/path/to/resume.pdf"
```

### 5. Analyze Candidate Fit

**Using the Web UI:**
1. Click on "Match & Analyze" in the navigation
2. Select a candidate from the dropdown
3. Select a client requirement from the dropdown
4. Click "Analyze Fit"
5. Wait for the AI to complete the analysis (may take 10-30 seconds)
6. View the fit score and detailed analysis

**Using the API:**
```bash
curl -X POST http://localhost:8080/api/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "candidateId": "candidate-uuid-here",
    "clientId": "client-uuid-here"
  }'
```

### 6. Optimize Resume (if fit score > 60)

**Using the Web UI:**
1. After analysis, if fit score is above 60, click "Optimize Resume"
2. The system will generate a tailored resume PDF
3. Download the optimized resume

**Using the API:**
```bash
curl -X POST http://localhost:8080/api/optimize-resume \
  -H "Content-Type: application/json" \
  -d '{
    "candidateId": "candidate-uuid-here",
    "clientId": "client-uuid-here"
  }'
```

Download the optimized resume:
```bash
curl -O http://localhost:8080/api/resume/modified/{candidateId}/{clientId}
```

### 7. Generate Interview Preparation

**Using the Web UI:**
1. Click on "Interview Prep" in the navigation
2. Select a candidate and client requirement
3. Click "Generate Interview Prep"
4. View technical questions, behavioral questions, talking points, and tips

**Using the API:**
```bash
curl -X POST http://localhost:8080/api/interview-prep \
  -H "Content-Type: application/json" \
  -d '{
    "candidateId": "candidate-uuid-here",
    "clientId": "client-uuid-here"
  }'
```

### 8. Complete Workflow

To run the entire workflow in one API call:

```bash
curl -X POST http://localhost:8080/api/workflow/process \
  -H "Content-Type: application/json" \
  -d '{
    "candidateId": "candidate-uuid-here",
    "clientId": "client-uuid-here"
  }'
```

This will:
1. Analyze candidate fit
2. Generate optimized resume (if fit score > 60)
3. Generate interview preparation questions
4. Return all results in one response

## Expected Results

### Client Requirement Parsing
Input:
```
"Looking for an intermediate Java developer with Java 17, Spring Boot, and AWS experience"
```

Expected output structure:
```json
{
  "id": "generated-uuid",
  "role": "Java Developer",
  "level": "intermediate",
  "requiredSkills": ["Java 17", "Spring Boot", "AWS"],
  "preferredSkills": [],
  "yearsOfExperience": 3,
  "rawRequirement": "Looking for...",
  "createdAt": "2026-01-22T..."
}
```

### Analysis Result
Expected output:
```json
{
  "id": "analysis-uuid",
  "candidateId": "candidate-uuid",
  "clientId": "client-uuid",
  "fitScore": 75,
  "matchedSkills": ["Java 17", "Spring Boot"],
  "missingSkills": ["AWS"],
  "skillEvidence": {
    "Java 17": "5 years of Java development experience including Java 17 features",
    "Spring Boot": "Extensive experience building microservices with Spring Boot"
  },
  "recommendations": [
    "Candidate should highlight AWS experience if available",
    "Consider AWS certification to strengthen profile"
  ],
  "analyzedAt": "2026-01-22T..."
}
```

### Interview Preparation
Expected output:
```json
{
  "id": "prep-uuid",
  "candidateId": "candidate-uuid",
  "clientId": "client-uuid",
  "technicalQuestions": [
    "Explain the new features in Java 17 and how you've used them",
    "Describe your experience with Spring Boot 3.x",
    "How would you design a microservices architecture using Spring Boot?"
  ],
  "behavioralQuestions": [
    "Tell me about a challenging project where you had to learn a new technology",
    "Describe a time when you had to optimize application performance"
  ],
  "talkingPoints": [
    "Emphasize hands-on experience with Java 17 features",
    "Discuss specific Spring Boot projects and their impact",
    "Highlight problem-solving abilities"
  ],
  "tips": [
    "Be prepared to discuss code examples",
    "Review Spring Boot best practices",
    "Be ready for live coding exercises"
  ],
  "generatedAt": "2026-01-22T..."
}
```

## Troubleshooting

### OpenAI API Errors
- Ensure your API key is valid and has sufficient credits
- Check if you have access to GPT-4o model
- Rate limiting: wait a few seconds between requests

### File Upload Issues
- Ensure PDF files are valid and under 10MB
- Check file permissions
- Verify the data directory is writable

### Analysis Takes Too Long
- OpenAI API calls can take 10-30 seconds
- Large resumes (>5 pages) may take longer
- Check your internet connection

### Missing Dependencies
If you see dependency errors:
```bash
mvn clean install -U
```

This forces Maven to update all dependencies.
