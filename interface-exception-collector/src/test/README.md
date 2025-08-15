# Interface Exception Collector Service - Test Suite

This directory contains the comprehensive test suite for the Interface Exception Collector Service, implementing all requirements from task 18.

## Test Structure

```
src/test/java/com/arcone/biopro/exception/collector/
├── ComprehensiveTestSuite.java           # Main test suite runner
├── integration/                          # Integration tests
│   └── ExceptionProcessingIntegrationTest.java
├── performance/                          # Performance tests
│   └── HighVolumeExceptionPerformanceTest.java
├── contract/                            # Contract tests
│   └── KafkaEventSchemaContractTest.java
├── e2e/                                 # End-to-end tests
│   └── EndToEndExceptionCollectorTest.java
├── load/                                # Load tests
│   └── ApiLoadTest.java
├── testutil/                            # Test utilities
│   ├── TestDataBuilder.java            # Fluent test data builders
│   └── TestFixtures.java               # Pre-configured test fixtures
└── config/                              # Test configuration
    └── TestConfiguration.java
```

## Test Categories

### 1. Integration Tests (`integration/`)
- **Purpose**: Test complete exception processing workflows
- **Coverage**: End-to-end flows from Kafka event consumption to database storage, API access, and retry operations
- **Infrastructure**: TestContainers (PostgreSQL, Redis), Embedded Kafka
- **Key Tests**:
  - Complete order exception processing workflow
  - Exception retry workflow with external service integration
  - Critical exception alerting workflow
  - Exception search and filtering workflow
  - Exception lifecycle state management workflow

### 2. Performance Tests (`performance/`)
- **Purpose**: Test high-volume exception scenarios
- **Coverage**: System behavior under load with large numbers of concurrent exceptions
- **Enabled**: Run with `-Dperformance.tests.enabled=true`
- **Key Tests**:
  - High volume exception processing (1000 events)
  - Concurrent API load (500 concurrent requests)
  - Database query performance under load
  - Memory usage under high load
  - System recovery after overload

### 3. Contract Tests (`contract/`)
- **Purpose**: Validate Kafka event schemas
- **Coverage**: All inbound and outbound events conform to expected schemas
- **Key Tests**:
  - Inbound event schema validation (OrderRejected, CollectionRejected, etc.)
  - Outbound event schema validation (ExceptionCaptured, ExceptionResolved, etc.)
  - Event serialization/deserialization
  - Event version compatibility

### 4. End-to-End Tests (`e2e/`)
- **Purpose**: Test the complete system with real infrastructure
- **Coverage**: Full system testing with TestContainers for PostgreSQL and Kafka
- **Infrastructure**: PostgreSQL, Redis, Kafka, WireMock for external services
- **Key Tests**:
  - Complete exception processing from event to resolution
  - Critical exception alert workflow
  - API comprehensive functionality
  - External service integration with fallback
  - System health and monitoring

### 5. Load Tests (`load/`)
- **Purpose**: Test API endpoints under concurrent load
- **Coverage**: System behavior under high concurrent API usage
- **Enabled**: Run with `-Dload.tests.enabled=true`
- **Key Tests**:
  - List exceptions API load (1000 concurrent requests)
  - Exception details API load (500 concurrent requests)
  - Search API load (300 concurrent requests)
  - Mixed API operations load (800 concurrent requests)
  - Acknowledgment API load (200 concurrent requests)

## Test Utilities

### TestDataBuilder (`testutil/TestDataBuilder.java`)
Fluent builder for creating test data objects with sensible defaults:

```java
// Create a basic exception
InterfaceException exception = TestDataBuilder.anInterfaceException()
    .transactionId("TEST-001")
    .severity(ExceptionSeverity.HIGH)
    .acknowledged()
    .build();

// Create an order rejected event
OrderRejectedEvent event = TestDataBuilder.anOrderRejectedEvent()
    .transactionId("TEST-002")
    .rejectedReason("Invalid customer data")
    .build();

// Create multiple exceptions
List<InterfaceException> exceptions = TestDataBuilder.createMultipleExceptions(10);
```

### TestFixtures (`testutil/TestFixtures.java`)
Pre-configured test data for common scenarios:

```java
// Get a new order exception
InterfaceException exception = TestFixtures.Exceptions.newOrderException();

// Get multiple retry attempts
List<RetryAttempt> attempts = TestFixtures.RetryAttempts.multipleRetryAttempts();

// Get test events
OrderRejectedEvent event = TestFixtures.Events.orderRejectedEvent();
```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Comprehensive Test Suite
```bash
mvn test -Dtest=ComprehensiveTestSuite
```

### Run with Performance Tests
```bash
mvn test -Dtest=ComprehensiveTestSuite -Dperformance.tests.enabled=true
```

### Run with Load Tests
```bash
mvn test -Dtest=ComprehensiveTestSuite -Dload.tests.enabled=true
```

### Run All Comprehensive Tests
```bash
mvn test -Dtest=ComprehensiveTestSuite -Dperformance.tests.enabled=true -Dload.tests.enabled=true
```

### Run Specific Test Categories
```bash
# Integration tests only
mvn test -Dtest="*IntegrationTest"

# Performance tests only
mvn test -Dtest="*PerformanceTest" -Dperformance.tests.enabled=true

# Contract tests only
mvn test -Dtest="*ContractTest"

# End-to-end tests only
mvn test -Dtest="*EndToEndTest"

# Load tests only
mvn test -Dtest="*LoadTest" -Dload.tests.enabled=true
```

## Test Infrastructure

### TestContainers
- **PostgreSQL**: Real database for integration testing
- **Redis**: Real cache for caching tests
- **Kafka**: Real message broker for event processing

### WireMock
- External service simulation
- Configurable responses for different scenarios
- Network failure simulation

### Embedded Kafka
- Lightweight Kafka for unit and integration tests
- Multiple topics and partitions
- Consumer and producer testing

## Test Configuration

### Application Properties
Test-specific configurations in `src/test/resources/application-test.yml`:
- H2 database for unit tests
- Embedded Kafka configuration
- Disabled security for testing
- Debug logging levels

### System Properties
- `performance.tests.enabled=true`: Enable performance tests
- `load.tests.enabled=true`: Enable load tests
- `spring.profiles.active=test`: Activate test profile

## Performance Benchmarks

### Expected Performance Metrics
- **Exception Processing**: >100 events/second
- **API Response Time**: <2 seconds average
- **Database Queries**: <1 second for filtered queries
- **Search Operations**: <2 seconds for full-text search
- **Memory Usage**: <500MB increase under load

### Load Test Thresholds
- **Success Rate**: >95% for all operations
- **Throughput**: >10 requests/second minimum
- **95th Percentile**: <5 seconds response time
- **Concurrent Users**: Support 100+ concurrent users

## Troubleshooting

### Common Issues

1. **TestContainers Startup Failures**
   - Ensure Docker is running
   - Check available memory (minimum 4GB recommended)
   - Verify network connectivity

2. **Performance Test Timeouts**
   - Increase test timeouts with `-Dtest.timeout=300`
   - Reduce test data size for slower environments
   - Check system resources

3. **Kafka Test Failures**
   - Ensure no other Kafka instances are running
   - Check port availability (9092, 9093)
   - Verify embedded Kafka configuration

4. **Database Connection Issues**
   - Check PostgreSQL TestContainer logs
   - Verify connection pool settings
   - Ensure proper cleanup between tests

### Debug Mode
Enable debug logging for troubleshooting:
```bash
mvn test -Dlogging.level.com.arcone.biopro=DEBUG -Dtest=ComprehensiveTestSuite
```

## Contributing

When adding new tests:

1. Follow the existing package structure
2. Use TestDataBuilder and TestFixtures for test data
3. Include proper test documentation
4. Add performance assertions where appropriate
5. Ensure tests are deterministic and isolated
6. Use meaningful test names and descriptions

## Test Coverage

The comprehensive test suite covers:

- ✅ Exception event consumption and processing
- ✅ Database operations and queries  
- ✅ REST API endpoints and responses
- ✅ External service integration
- ✅ Event publishing and correlation
- ✅ Error handling and resilience
- ✅ Performance under load
- ✅ Data consistency and integrity
- ✅ Schema validation and compatibility
- ✅ System health and monitoring

This test suite fulfills all requirements from task 18: "Create comprehensive test suite" including integration tests, performance tests, contract tests, end-to-end tests with TestContainers, load testing scenarios, and test data builders and fixtures.