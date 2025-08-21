# Interface Exception Collector GraphQL API - Bruno Collection

This Bruno collection provides comprehensive testing capabilities for the Interface Exception Collector GraphQL API.

## Setup Instructions

1. **Import Collection**: Import this collection into Bruno
2. **Configure Environment**: 
   - Select the "Local" environment for local development
   - Update the `api-token` variable with a valid JWT token
3. **Generate JWT Token**:
   ```bash
   node generate-jwt-correct-secret.js "test-user" "ADMIN"
   ```
4. **Update Token**: Copy the generated token to the `api-token` environment variable

## Collection Structure

### Queries (5 operations)
- **Get Exceptions**: Paginated list with filtering
- **Get Exception by ID**: Detailed exception information
- **Get Exception Summary**: Aggregated statistics
- **Search Exceptions**: Full-text search capabilities
- **Get System Health**: System status monitoring

### Mutations (6 operations)
- **Retry Exception**: Single exception retry
- **Bulk Retry Exceptions**: Multiple exception retry
- **Acknowledge Exception**: Exception acknowledgment
- **Bulk Acknowledge Exceptions**: Multiple exception acknowledgment
- **Resolve Exception**: Mark exception as resolved
- **Cancel Retry**: Cancel ongoing retry operation

### Subscriptions (3 operations)
- **Exception Updates**: Real-time exception events
- **Summary Updates**: Real-time statistics updates
- **Retry Status Updates**: Real-time retry status changes

### Advanced Queries (3 operations)
- **Complex Filtering**: Advanced filtering examples
- **Custom Time Range Summary**: Custom date range statistics
- **Test Data Check**: Verify test data availability

## Environment Variables

- `graphql-endpoint`: GraphQL API endpoint URL
- `api-token`: JWT authentication token

## Test Data

The collection includes realistic test data in variables:
- Transaction IDs: `test-transaction-123`, `test-transaction-456`, etc.
- Customer IDs: `CUST-MOUNT-SINAI-001`, `CUST-MAYO-CLINIC-002`
- Location Codes: `HOSP-NYC-001`, `HOSP-MN-001`

## Usage Tips

1. **Start with Queries**: Test basic data retrieval first
2. **Use Test Data Check**: Verify test data exists before running other operations
3. **Authentication**: Ensure your JWT token is valid and has appropriate permissions
4. **Subscriptions**: Require WebSocket support - ensure your GraphQL endpoint supports subscriptions
5. **Error Handling**: Check the `errors` field in mutation responses for detailed error information

## Troubleshooting

- **401 Unauthorized**: Check your JWT token validity
- **403 Forbidden**: Ensure your token has the required role (ADMIN recommended for testing)
- **Connection Issues**: Verify the GraphQL endpoint is running and accessible
- **Subscription Failures**: Ensure WebSocket support is enabled on the server

## Support

For issues or questions, refer to the main project documentation or contact the development team.
