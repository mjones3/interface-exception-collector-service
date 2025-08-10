#!/bin/bash

# Test the summary endpoint
echo "Testing /api/v1/exceptions/summary endpoint..."

# Make the request and capture both response and status code
response=$(curl -s -w "HTTPSTATUS:%{http_code}" "http://localhost:8080/api/v1/exceptions/summary" 2>/dev/null)

# Extract the status code
http_code=$(echo "$response" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)

# Extract the response body (remove the status code part)
response_body=$(echo "$response" | sed 's/HTTPSTATUS:[0-9]*$//')

echo "HTTP Status Code: $http_code"
echo "Response Body:"
echo "$response_body" | head -20

if [ "$http_code" = "200" ]; then
    echo "✅ Summary endpoint is working!"
else
    echo "❌ Summary endpoint returned error code: $http_code"
fi