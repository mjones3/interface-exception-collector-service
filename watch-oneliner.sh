#!/bin/bash

# One-liner GraphQL watcher
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"

while true; do
  echo "=== $(date '+%H:%M:%S') ==="
  curl -s -X POST http://localhost:8080/graphql \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"query": "{ exceptions(pagination: { first: 5 }) { totalCount edges { node { transactionId status severity timestamp } } } }"}' \
    | jq -r '.data.exceptions | "Total: " + (.totalCount | tostring) + "\nExceptions:" + (.edges[] | "\n  " + .node.transactionId + " | " + .node.status + " | " + .node.severity)'
  sleep 5
done