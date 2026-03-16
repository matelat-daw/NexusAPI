#!/bin/bash
# NEXUSAPI - QUICK START DEPLOYMENT SCRIPT
# Copy and run these commands to deploy NexusAPI

echo "=========================================="
echo "NEXUSAPI - DEPLOYMENT QUICK START"
echo "=========================================="

# ========== STEP 1: SET ENVIRONMENT VARIABLES ==========
echo ""
echo "STEP 1: Setting environment variables..."
echo "Copy and run these commands in your terminal:"
echo ""

cat << 'EOF'
# Database Configuration
export DB_URL="jdbc:sqlserver://your-host:1433;databaseName=Nexus;encrypt=true;trustServerCertificate=false"
export DB_USER="sa"
export DB_PASSWORD="your-secure-password"

# JWT Configuration
export JWT_ISSUER="NexusAstralisProject"
export JWT_AUDIENCE="NexusAstralis"
export JWT_SECRET="your-secret-key-minimum-32-characters"
export JWT_EXPIRATION_MINUTES="120"

# Mail Configuration
export MAIL_HOST="smtp.gmail.com"
export MAIL_PORT="587"
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-app-password"

# CORS Configuration
export CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://api.yourdomain.com"
EOF

echo ""
echo "⚠️  IMPORTANT: Update the values above with your actual credentials!"
echo ""

# ========== STEP 2: BUILD THE PROJECT ==========
echo ""
echo "STEP 2: Building the project..."
echo "Command:"
echo "mvn clean package -DskipTests"
echo ""

# ========== STEP 3: RUN THE APPLICATION ==========
echo ""
echo "STEP 3: Running the application..."
echo "Command (Local Development):"
echo "java -jar target/NexusAPI-0.0.1-SNAPSHOT.jar"
echo ""

# ========== STEP 4: VERIFY HEALTH ==========
echo ""
echo "STEP 4: Verify the application is running..."
echo "Command:"
echo "curl http://localhost:8080/actuator/health"
echo ""
echo "Expected response:"
cat << 'EOF'
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
EOF

echo ""

# ========== STEP 5: TEST AN ENDPOINT ==========
echo ""
echo "STEP 5: Test authentication endpoint..."
echo "Command (should fail with validation error):"
echo 'curl -X POST http://localhost:8080/api/Auth/Login \'
echo '  -H "Content-Type: application/json" \'
echo '  -d '"'"'{"email":"invalid","password":"short"}'"'"''
echo ""

# ========== DOCKER DEPLOYMENT ==========
echo ""
echo "========== DOCKER DEPLOYMENT =========="
echo ""
echo "Build Docker image:"
echo "docker build -t nexusapi:latest ."
echo ""
echo "Run Docker container:"
echo 'docker run -p 8080:8080 \'
echo '  -e DB_URL="..." \'
echo '  -e DB_USER="sa" \'
echo '  -e DB_PASSWORD="..." \'
echo '  -e JWT_SECRET="..." \'
echo '  nexusapi:latest'
echo ""

# ========== KUBERNETES DEPLOYMENT ==========
echo ""
echo "========== KUBERNETES DEPLOYMENT =========="
echo ""
echo "Create ConfigMap for configuration:"
cat << 'EOF'
kubectl create configmap nexusapi-config \
  --from-literal=MAIL_HOST=smtp.gmail.com \
  --from-literal=MAIL_PORT=587
EOF
echo ""
echo "Create Secret for sensitive data:"
cat << 'EOF'
kubectl create secret generic nexusapi-secret \
  --from-literal=DB_PASSWORD=your-password \
  --from-literal=JWT_SECRET=your-secret-key \
  --from-literal=MAIL_PASSWORD=your-mail-password
EOF
echo ""

# ========== DATABASE SETUP ==========
echo ""
echo "========== DATABASE OPTIMIZATION =========="
echo ""
echo "Run these SQL commands on your SQL Server to optimize the database:"
echo "1. Open: DATABASE_OPTIMIZATION.sql"
echo "2. Execute in SQL Server Management Studio"
echo ""

# ========== MONITORING ==========
echo ""
echo "========== MONITORING =========="
echo ""
echo "Health Status:"
echo "curl http://localhost:8080/actuator/health"
echo ""
echo "Metrics:"
echo "curl http://localhost:8080/actuator/metrics"
echo ""
echo "Prometheus Format:"
echo "curl http://localhost:8080/actuator/prometheus"
echo ""
echo "API Documentation:"
echo "http://localhost:8080/swagger-ui.html"
echo ""

# ========== VERIFY SETUP ==========
echo ""
echo "========== VERIFICATION CHECKLIST =========="
echo ""
echo "☐ Environment variables are set correctly"
echo "☐ Database is accessible and credentials are correct"
echo "☐ JWT secret is strong (32+ characters)"
echo "☐ CORS origins are configured correctly"
echo "☐ Mail credentials are valid (Gmail needs app-specific password)"
echo "☐ Firewall allows connections to port 8080"
echo "☐ Database has proper indexes created"
echo ""

# ========== TROUBLESHOOTING ==========
echo ""
echo "========== TROUBLESHOOTING =========="
echo ""
echo "If health check fails:"
echo "1. Check logs: tail -100 logs/application.log"
echo "2. Verify environment variables: env | grep -E '(DB_|JWT_|MAIL_)'"
echo "3. Test database connection: sqlcmd -S your-host -U sa -P password"
echo ""

# ========== FINAL COMMANDS ==========
echo ""
echo "========== SUMMARY =========="
echo ""
echo "1. Set environment variables (copy from above)"
echo "2. Run: mvn clean package -DskipTests"
echo "3. Run: java -jar target/NexusAPI-0.0.1-SNAPSHOT.jar"
echo "4. Test: curl http://localhost:8080/actuator/health"
echo "5. View docs: http://localhost:8080/swagger-ui.html"
echo ""
echo "✅ Deployment complete!"
echo ""

