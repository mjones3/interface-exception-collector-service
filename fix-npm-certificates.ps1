#!/usr/bin/env pwsh

# Fix npm certificate issues for corporate networks

Write-Host "Fixing npm Certificate Issues" -ForegroundColor Green
Write-Host "=============================" -ForegroundColor Green

Write-Host "`n🔍 Diagnosing npm configuration..." -ForegroundColor Yellow

# Show current npm config
Write-Host "`nCurrent npm registry:"
try {
    npm config get registry
} catch {
    Write-Host "Error getting npm config: $_" -ForegroundColor Red
}

Write-Host "`n🔧 Applying fixes..." -ForegroundColor Cyan

# Fix 1: Use official npm registry
Write-Host "1. Setting official npm registry..."
try {
    npm config set registry https://registry.npmjs.org/
    Write-Host "✅ Registry set to official npmjs.org" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to set registry: $_" -ForegroundColor Red
}

# Fix 2: Disable strict SSL (for corporate networks)
Write-Host "`n2. Disabling strict SSL verification..."
try {
    npm config set strict-ssl false
    Write-Host "✅ Strict SSL disabled" -ForegroundColor Green
    Write-Host "⚠️  Note: This is less secure but needed for corporate networks" -ForegroundColor Yellow
} catch {
    Write-Host "❌ Failed to disable strict SSL: $_" -ForegroundColor Red
}

# Fix 3: Clear npm cache
Write-Host "`n3. Clearing npm cache..."
try {
    npm cache clean --force
    Write-Host "✅ npm cache cleared" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to clear cache: $_" -ForegroundColor Red
}

# Fix 4: Check for corporate proxy
Write-Host "`n4. Checking for proxy settings..."
$proxyEnv = $env:HTTP_PROXY -or $env:HTTPS_PROXY -or $env:http_proxy -or $env:https_proxy

if ($proxyEnv) {
    Write-Host "⚠️  Proxy detected in environment variables" -ForegroundColor Yellow
    Write-Host "HTTP_PROXY: $env:HTTP_PROXY"
    Write-Host "HTTPS_PROXY: $env:HTTPS_PROXY"
    
    if ($env:HTTP_PROXY) {
        Write-Host "Setting npm proxy..."
        npm config set proxy $env:HTTP_PROXY
        npm config set https-proxy $env:HTTP_PROXY
        Write-Host "✅ Proxy configured for npm" -ForegroundColor Green
    }
} else {
    Write-Host "✅ No proxy environment variables detected" -ForegroundColor Green
}

Write-Host "`n🧪 Testing npm connection..." -ForegroundColor Cyan
try {
    Write-Host "Testing connection to npm registry..."
    $testResult = npm ping 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ npm connection successful!" -ForegroundColor Green
    } else {
        Write-Host "❌ npm connection failed: $testResult" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ npm ping failed: $_" -ForegroundColor Red
}

Write-Host "`n🎯 Now try installing ws:" -ForegroundColor Green
Write-Host "npm install ws"

Write-Host "`n📋 Alternative Solutions:" -ForegroundColor Yellow
Write-Host "If npm still doesn't work:"
Write-Host "1. Use GraphiQL (no dependencies): http://localhost:8080/graphiql"
Write-Host "2. Use the HTML test client: graphql-subscription-test.html"
Write-Host "3. Ask your IT team about npm/Node.js proxy settings"
Write-Host "4. Use yarn instead: yarn add ws"

Write-Host "`n🔒 Security Note:" -ForegroundColor Red
Write-Host "The 'strict-ssl false' setting is less secure."
Write-Host "Re-enable it later with: npm config set strict-ssl true"

Write-Host "`n✅ Configuration complete!" -ForegroundColor Green