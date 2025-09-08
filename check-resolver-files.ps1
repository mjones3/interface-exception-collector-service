# Check GraphQL Resolver Files
Write-Host "CHECKING RESOLVER FILES" -ForegroundColor Blue

Write-Host "1. Checking SimpleTestResolver..." -ForegroundColor Cyan
if (Test-Path "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/SimpleTestResolver.java") {
    Write-Host "✅ SimpleTestResolver exists" -ForegroundColor Green
} else {
    Write-Host "❌ SimpleTestResolver missing" -ForegroundColor Red
}

Write-Host "2. Checking SummaryQueryResolver..." -ForegroundColor Cyan
if (Test-Path "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/resolver/SummaryQueryResolver.java") {
    Write-Host "✅ SummaryQueryResolver exists" -ForegroundColor Green
} else {
    Write-Host "❌ SummaryQueryResolver missing" -ForegroundColor Red
}

Write-Host "3. Checking ExceptionSubscriptionResolver..." -ForegroundColor Cyan
if (Test-Path "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/api/graphql/resolver/ExceptionSubscriptionResolver.java") {
    Write-Host "✅ ExceptionSubscriptionResolver exists" -ForegroundColor Green
} else {
    Write-Host "❌ ExceptionSubscriptionResolver missing" -ForegroundColor Red
}

Write-Host "4. Checking GraphQL schema..." -ForegroundColor Cyan
if (Test-Path "interface-exception-collector/src/main/resources/graphql/schema.graphqls") {
    Write-Host "✅ GraphQL schema exists" -ForegroundColor Green
} else {
    Write-Host "❌ GraphQL schema missing" -ForegroundColor Red
}

Write-Host "5. Checking Maven dependencies..." -ForegroundColor Cyan
$pomContent = Get-Content "interface-exception-collector/pom.xml" -Raw
if ($pomContent -like "*spring-boot-starter-graphql*") {
    Write-Host "✅ GraphQL starter dependency found" -ForegroundColor Green
} else {
    Write-Host "❌ GraphQL starter dependency missing" -ForegroundColor Red
}

Write-Host "RESOLVER FILES CHECK COMPLETE" -ForegroundColor Blue