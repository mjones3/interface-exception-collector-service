# Run application without database
Write-Host "Starting application without database..." -ForegroundColor Green

# Clean any locks
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

# Start application
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=nodatabase --server.port=8095"
