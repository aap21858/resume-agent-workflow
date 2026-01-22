#!/bin/bash

# Example workflow execution using curl

# Configuration
API_URL="http://localhost:8080/api/workflow/execute"

# Example request payload
cat > /tmp/workflow-request.json << 'EOF'
{
  "clientRequirement": "Intermediate Java developer with 3-5 years experience. Must have Java 17, Spring Boot 3.x, and AWS (EC2, S3, Lambda). Experience with microservices architecture, RESTful APIs, and SQL databases. Preferred: Docker, Kubernetes, CI/CD, NoSQL.",
  "candidateResumePath": "./data/resumes/original/john-doe.pdf",
  "clientId": "client-techcorp-001",
  "candidateId": "candidate-johndoe",
  "generateModifiedResume": true,
  "generateInterviewPrep": true
}
EOF

echo "Executing workflow..."
echo "Request payload:"
cat /tmp/workflow-request.json | jq .

echo ""
echo "Sending request to $API_URL"
echo ""

curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d @/tmp/workflow-request.json \
  | jq .

rm /tmp/workflow-request.json
