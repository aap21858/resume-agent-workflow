# Resume Agent Workflow System

An AI-powered agentic workflow system for consultant companies to optimize candidate resumes based on client requirements, analyze candidate fit, and prepare interview questions.

## Features

- **Multi-Agent System**: Four specialized AI agents working together
  - Requirements Parser Agent: Extracts structured information from client requirements
  - Resume Analyzer Agent: Analyzes candidate fit against requirements
  - Resume Modifier Agent: Tailors resumes to highlight relevant experience
  - Interview Prep Agent: Generates interview questions and preparation materials

- **File-Based Storage**: JSON files for all data (no database required)
- **PDF Processing**: Read and generate PDF resumes
- **Web UI**: User-friendly interface with Bootstrap styling
- **REST API**: Complete API for all operations

## Technology Stack

- **Java 17+**
- **Spring Boot 3.2.1**
- **Spring AI** with OpenAI integration
- **Apache PDFBox** for PDF processing
- **Thymeleaf** + Bootstrap for web UI
- **File-based JSON storage**

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- OpenAI API key

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/aap21858/resume-agent-workflow.git
cd resume-agent-workflow
```

### 2. Set OpenAI API Key

Set your OpenAI API key as an environment variable:

**Linux/Mac:**
```bash
export OPENAI_API_KEY=your-api-key-here
```

**Windows (CMD):**
```cmd
set OPENAI_API_KEY=your-api-key-here
```

**Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY="your-api-key-here"
```

### 3. Build the Application

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Usage Workflow

### 1. Create Client Requirements

1. Navigate to **Clients** page
2. Enter a natural language requirement (e.g., "Looking for an intermediate Java developer with Java 17, Spring Boot, and AWS experience")
3. Click **Create Requirement**
4. The AI will parse and structure the requirement

### 2. Upload Candidate Resumes

1. Navigate to **Upload Resume** page
2. Fill in candidate name and email
3. Select a PDF resume file
4. Click **Upload Resume**
5. The system will extract text and identify skills

### 3. Analyze Candidate Fit

1. Navigate to **Match & Analyze** page
2. Select a candidate and client requirement
3. Click **Analyze Fit**
4. The AI will analyze the fit and provide a score (0-100)
5. View detailed analysis including matched/missing skills

### 4. Optimize Resume (Optional)

1. If fit score > 60, click **Optimize Resume**
2. The system will generate a tailored resume PDF
3. Download the optimized resume

### 5. Generate Interview Preparation

1. Navigate to **Interview Prep** page
2. Select a candidate and client requirement
3. Click **Generate Interview Prep**
4. View technical questions, behavioral questions, talking points, and tips

## API Documentation

### Client Requirements

- **POST** `/api/clients` - Create client requirement
  ```json
  {
    "requirement": "Looking for an intermediate Java developer with Java 17, Spring Boot, and AWS"
  }
  ```

- **GET** `/api/clients` - List all clients
- **GET** `/api/clients/{clientId}` - Get specific client

### Candidates

- **POST** `/api/candidates/upload` - Upload resume (multipart/form-data)
  - `file`: PDF file
  - `name`: Candidate name
  - `email`: Candidate email

- **GET** `/api/candidates` - List all candidates
- **GET** `/api/candidates/{candidateId}` - Get specific candidate

### Analysis

- **POST** `/api/analyze` - Analyze candidate fit
  ```json
  {
    "candidateId": "candidate-uuid",
    "clientId": "client-uuid"
  }
  ```

- **GET** `/api/analysis/{candidateId}/{clientId}` - Get analysis results
- **GET** `/api/analyses` - List all analyses

### Resume Optimization

- **POST** `/api/optimize-resume` - Generate optimized resume
  ```json
  {
    "candidateId": "candidate-uuid",
    "clientId": "client-uuid"
  }
  ```

- **GET** `/api/resume/original/{candidateId}` - Download original resume
- **GET** `/api/resume/modified/{candidateId}/{clientId}` - Download modified resume

### Interview Preparation

- **POST** `/api/interview-prep` - Generate interview questions
  ```json
  {
    "candidateId": "candidate-uuid",
    "clientId": "client-uuid"
  }
  ```

- **GET** `/api/interview-prep/{candidateId}/{clientId}` - Get interview prep

### Complete Workflow

- **POST** `/api/workflow/process` - Execute full workflow (analyze + optimize + interview prep)
  ```json
  {
    "candidateId": "candidate-uuid",
    "clientId": "client-uuid"
  }
  ```

## File Structure

```
data/
├── candidates/
│   ├── {candidate-id}.json          # Candidate metadata
│   └── resumes/
│       ├── original/
│       │   └── {candidate-id}.pdf   # Original resume
│       └── modified/
│           └── {candidate-id}_{client-id}.pdf  # Tailored resume
├── clients/
│   └── {client-id}.json             # Client requirements
├── analyses/
│   └── {candidate-id}_{client-id}.json  # Analysis results
└── interview-prep/
    └── {candidate-id}_{client-id}.json  # Interview questions
```

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# OpenAI Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o
spring.ai.openai.chat.options.temperature=0.7

# File Storage
app.storage.base-path=./data

# Server
server.port=8080

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Example Workflow

1. **Create a client requirement:**
   ```
   "Looking for a senior Java developer with 5+ years experience in Java 17, 
   Spring Boot 3.x, microservices, AWS, and Docker"
   ```

2. **Upload a candidate resume** (PDF file)

3. **Run analysis** - Get a fit score and gap analysis

4. **If fit score > 60**: Generate optimized resume that emphasizes relevant skills

5. **Generate interview prep** - Get customized questions and talking points

## Architecture

The system uses a multi-agent architecture where specialized agents handle different tasks:

- **WorkflowOrchestrator**: Coordinates the overall workflow
- **Agents**: Specialized AI agents for parsing, analyzing, modifying, and preparing
- **Services**: Business logic layer
- **Controllers**: REST API and web endpoints
- **Utils**: PDF and JSON processing utilities

## Troubleshooting

### Application won't start
- Ensure Java 17+ is installed: `java -version`
- Ensure OPENAI_API_KEY is set: `echo $OPENAI_API_KEY`
- Check if port 8080 is available

### OpenAI API errors
- Verify your API key is valid
- Check your OpenAI account has sufficient credits
- Ensure you have access to the GPT-4o model

### PDF processing errors
- Ensure uploaded files are valid PDFs
- Check file size is under 10MB
- Verify PDFBox dependencies are installed

## Development

### Running tests
```bash
mvn test
```

### Building for production
```bash
mvn clean package
java -jar target/resume-agent-workflow-1.0.0.jar
```

## License

MIT License

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## Support

For issues and questions, please open a GitHub issue.