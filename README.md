# Resume Agent Workflow System

An AI-powered agentic workflow system for optimizing candidate resumes based on client requirements, analyzing candidate fit, and preparing interview materials.

## 🎯 Overview

This Spring Boot application provides a complete workflow for consulting companies to:
1. Parse and structure client job requirements using AI
2. Analyze candidate resumes against requirements with fit scoring (0-100 scale)
3. Generate tailored resumes highlighting relevant skills and experience
4. Create personalized interview preparation materials

## ✨ Features

- **AI-Powered Agents**: Four specialized agents using OpenAI GPT-4
  - Client Requirements Parser Agent
  - Resume Analyzer Agent  
  - Resume Modifier Agent
  - Interview Prep Agent

- **PDF Processing**: Read and generate PDF resumes using Apache PDFBox

- **File-Based Storage**: JSON file storage for all data (no database required)

- **Modern Web UI**: Thymeleaf-based interface for easy workflow management

- **REST API**: Complete RESTful API for programmatic access

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring AI** for OpenAI integration
- **Apache PDFBox 3.0.1** for PDF processing
- **Thymeleaf** for web templates
- **Jackson** for JSON processing
- **Lombok** for boilerplate reduction

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- OpenAI API Key

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/aap21858/resume-agent-workflow.git
cd resume-agent-workflow
```

### 2. Configure OpenAI API Key

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Edit `.env` and add your OpenAI API key:

```
OPENAI_API_KEY=your-actual-api-key-here
```

### 3. Build the Application

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or with environment variables:

```bash
OPENAI_API_KEY=your-key mvn spring-boot:run
```

### 5. Access the Application

Open your browser and navigate to:
- **Web UI**: http://localhost:8080
- **API Health Check**: http://localhost:8080/api/workflow/health

## 📁 Project Structure

```
src/main/java/com/resume/agent/
├── ResumeAgentApplication.java          # Main application class
├── config/
│   ├── FileStorageConfig.java           # File storage configuration
│   └── WorkflowConfig.java              # Workflow settings
├── agents/
│   ├── ClientRequirementsParserAgent.java
│   ├── CandidateProfileParserAgent.java
│   ├── ResumeAnalyzerAgent.java
│   ├── ResumeModifierAgent.java
│   └── InterviewPrepAgent.java
├── orchestrator/
│   └── WorkflowOrchestrator.java        # Main workflow coordinator
├── service/
│   ├── PDFProcessingService.java        # PDF read/write operations
│   ├── FileStorageService.java          # JSON file management
│   └── OpenAIService.java               # OpenAI API wrapper
├── controller/
│   ├── WorkflowController.java          # REST API endpoints
│   ├── UploadController.java            # File upload/download
│   └── WebController.java               # Web page routes
├── model/
│   ├── ClientRequirement.java
│   ├── CandidateProfile.java
│   ├── AnalysisResult.java
│   └── InterviewPrep.java
└── dto/
    ├── WorkflowRequest.java
    └── WorkflowResult.java
```

## 📂 Data Storage Structure

The application creates the following directory structure:

```
data/
├── requirements/                         # Parsed client requirements
│   └── {client-id}-{timestamp}.json
├── candidates/                           # Candidate profiles
│   └── {candidate-id}-profile.json
├── resumes/
│   ├── original/                        # Original uploaded resumes
│   │   └── {candidate-id}.pdf
│   └── modified/                        # AI-tailored resumes
│       └── {candidate-id}-{client-id}.pdf
├── analysis/                            # Fit analysis results
│   └── {candidate-id}-{client-id}-analysis.json
└── interview-prep/                      # Interview materials
    └── {candidate-id}-{client-id}-prep.json
```

## 🔄 Workflow Execution

### Via Web UI

1. **Upload Resume**: Navigate to `/upload` and upload a candidate's PDF resume
2. **Execute Workflow**: Go to `/workflow` and provide:
   - Client job requirement (plain text)
   - Resume file path (from upload step)
3. **View Results**: Check the analysis, modified resume, and interview prep

### Via REST API

```bash
curl -X POST http://localhost:8080/api/workflow/execute \
  -H "Content-Type: application/json" \
  -d '{
    "clientRequirement": "Intermediate Java developer with Java 17, Spring Boot, and AWS",
    "candidateResumePath": "./data/resumes/original/candidate-123.pdf",
    "generateModifiedResume": true,
    "generateInterviewPrep": true
  }'
```

### Response Example

```json
{
  "workflowId": "uuid",
  "success": true,
  "message": "Workflow completed successfully",
  "clientRequirement": { ... },
  "candidateProfile": { ... },
  "analysisResult": {
    "fitScore": 85,
    "recommendForInterview": true,
    "strengths": ["Strong Java experience", "AWS certified"],
    "gaps": ["Limited Spring Boot 3.x experience"]
  },
  "modifiedResumePath": "./data/resumes/modified/candidate-123-client-456.pdf",
  "interviewPrep": { ... },
  "executionTimeMs": 25000,
  "status": "COMPLETED"
}
```

## 🔧 Configuration

Edit `src/main/resources/application.yml`:

```yaml
resume-agent:
  storage:
    base-path: ./data                    # Storage directory
  pdf:
    max-file-size: 5242880               # 5MB max file size
  workflow:
    fit-score-threshold: 60              # Minimum score for resume modification
```

## 📡 API Endpoints

### Workflow Operations

- `POST /api/workflow/execute` - Execute complete workflow
- `GET /api/workflow/health` - Health check

### File Operations

- `POST /api/upload/resume` - Upload resume PDF
- `GET /api/upload/download/resume/{candidateId}/{clientId}` - Download modified resume

### Web Pages

- `GET /` - Home page
- `GET /upload` - Resume upload page
- `GET /workflow` - Workflow execution page
- `GET /dashboard` - Dashboard (overview)
- `GET /requirements` - Requirements management
- `GET /analysis` - Analysis results

## 🧪 Example Usage

### Example 1: Complete Workflow

```java
WorkflowRequest request = WorkflowRequest.builder()
    .clientRequirement("Senior Java developer with 5+ years experience in Spring Boot, microservices, and AWS")
    .candidateResumePath("./data/resumes/original/john-doe.pdf")
    .generateModifiedResume(true)
    .generateInterviewPrep(true)
    .build();

WorkflowResult result = workflowOrchestrator.execute(request);

if (result.isSuccess() && result.getAnalysisResult().getFitScore() >= 60) {
    System.out.println("Candidate is a good fit!");
    System.out.println("Modified resume: " + result.getModifiedResumePath());
}
```

### Example 2: Client Requirement

```text
Position: Intermediate Java Developer

Requirements:
- 3+ years of Java development experience
- Strong knowledge of Java 17 features
- Experience with Spring Boot 3.x
- AWS services (EC2, S3, Lambda)
- Microservices architecture
- RESTful API design
- Docker and Kubernetes
- SQL and NoSQL databases

Preferred:
- CI/CD pipelines
- React or Angular
- Agile/Scrum experience
```

## 🔒 Security Considerations

- API keys are stored in environment variables (never committed to Git)
- File upload validation (type, size)
- Path traversal protection in file operations
- Input validation on all endpoints

## 🐛 Troubleshooting

### Common Issues

1. **OpenAI API Errors**
   - Verify API key is correct
   - Check API quota/rate limits
   - Ensure internet connectivity

2. **PDF Processing Errors**
   - Validate PDF is not corrupted
   - Check file size limits
   - Ensure sufficient disk space

3. **Build Failures**
   - Verify Java 17 is installed
   - Clear Maven cache: `mvn clean`
   - Check dependencies are accessible

## 📝 Development

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package
java -jar target/resume-agent-workflow-1.0.0-SNAPSHOT.jar
```

## 🤝 Contributing

This is a demonstration project. For production use, consider:
- Adding authentication/authorization
- Implementing database storage
- Adding comprehensive test coverage
- Implementing rate limiting
- Adding monitoring and observability

## 📄 License

This project is provided as-is for demonstration purposes.

## 🙏 Acknowledgments

- Spring AI for OpenAI integration
- Apache PDFBox for PDF processing
- OpenAI for GPT-4 capabilities

---

**Note**: This application requires an active OpenAI API key. API usage will incur costs based on OpenAI's pricing.