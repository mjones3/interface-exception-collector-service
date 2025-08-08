# Contributing to Interface Exception Collector Service

Thank you for your interest in contributing to the Interface Exception Collector Service! This document provides guidelines and standards for contributing to this project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Documentation Standards](#documentation-standards)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors, regardless of background, experience level, or identity.

### Expected Behavior

- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what is best for the community and the project
- Show empathy towards other community members

### Unacceptable Behavior

- Harassment, discrimination, or offensive comments
- Personal attacks or trolling
- Publishing private information without permission
- Any conduct that would be inappropriate in a professional setting

## Getting Started

### Prerequisites

Before contributing, ensure you have:

- Java 21+ installed
- Maven 3.8+ installed
- Docker and Docker Compose
- Git configured with your name and email
- IDE with Java support (IntelliJ IDEA recommended)

### Setting Up Development Environment

1. **Fork and clone the repository**:
   ```bash
   git clone https://github.com/your-username/interface-exception-collector-service.git
   cd interface-exception-collector-service
   ```

2. **Set up local infrastructure**:
   ```bash
   docker-compose up -d
   ./scripts/run-migrations.sh
   ./scripts/create-kafka-topics.sh
   ```

3. **Run the application**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

4. **Verify setup**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

## Development Workflow

### Branch Naming Convention

Use descriptive branch names with prefixes:

- `feature/AOA-123-add-retry-functionality` - New features
- `bugfix/AOA-456-fix-kafka-consumer-lag` - Bug fixes
- `hotfix/AOA-789-critical-security-patch` - Critical fixes
- `refactor/AOA-101-improve-error-handling` - Code refactoring
- `docs/AOA-202-update-api-documentation` - Documentation updates

### Commit Message Format

Follow the conventional commit format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Example:**
```
feat(retry): add exponential backoff for retry operations

Implement exponential backoff strategy for retry operations to improve
resilience when external services are temporarily unavailable.

- Add RetryBackoffStrategy interface
- Implement ExponentialBackoffStrategy
- Update RetryService to use configurable backoff
- Add unit tests for backoff calculations

Closes #123
```

### Development Process

1. **Create a feature branch** from `main`
2. **Make your changes** following coding standards
3. **Write tests** for new functionality
4. **Run the full test suite** to ensure nothing breaks
5. **Update documentation** if needed
6. **Submit a pull request** with a clear description

## Coding Standards

### Java Code Style

#### General Principles

- Follow **Clean Code** principles
- Use **SOLID** design principles
- Prefer **composition over inheritance**
- Write **self-documenting code** with meaningful names
- Keep methods **small and focused** (max 20 lines)
- Limit **cyclomatic complexity** (max 10)

#### Naming Conventions

```java
// Classes: PascalCase
public class ExceptionProcessingService { }

// Methods and variables: camelCase
public void processException(InterfaceException exception) { }
private String transactionId;

// Constants: UPPER_SNAKE_CASE
private static final String DEFAULT_STATUS = "NEW";
private static final int MAX_RETRY_ATTEMPTS = 3;

// Packages: lowercase with dots
com.arcone.biopro.exception.collector.domain.entity
```

#### Code Formatting

- **Indentation**: 4 spaces (no tabs)
- **Line length**: Maximum 120 characters
- **Braces**: Opening brace on same line
- **Imports**: Group and sort imports, remove unused imports

```java
// Good
public class ExceptionService {
    private final ExceptionRepository repository;
    
    public ExceptionService(ExceptionRepository repository) {
        this.repository = repository;
    }
    
    public void processException(InterfaceException exception) {
        if (exception == null) {
            throw new IllegalArgumentException("Exception cannot be null");
        }
        
        repository.save(exception);
    }
}
```

#### Documentation

- **Public APIs**: Always document with Javadoc
- **Complex logic**: Add inline comments explaining the "why"
- **TODOs**: Include ticket number and description

```java
/**
 * Processes interface exceptions and applies business rules for categorization.
 * 
 * @param exception the exception to process, must not be null
 * @throws IllegalArgumentException if exception is null
 * @throws ExceptionProcessingException if processing fails
 */
public void processException(InterfaceException exception) {
    // TODO: AOA-456 - Add validation for duplicate transaction IDs
    validateException(exception);
    
    // Apply severity rules based on exception type and reason
    ExceptionSeverity severity = determineSeverity(exception);
    exception.setSeverity(severity);
    
    repository.save(exception);
}
```

### Architecture Guidelines

#### Layer Responsibilities

- **API Layer**: Handle HTTP requests, validation, and response formatting
- **Application Layer**: Orchestrate business operations and use cases
- **Domain Layer**: Core business logic, entities, and domain rules
- **Infrastructure Layer**: External integrations and technical concerns

#### Dependency Rules

- **Inward dependencies only**: Outer layers depend on inner layers
- **No circular dependencies**: Use interfaces to break cycles
- **Dependency injection**: Use Spring's DI container

```java
// Good: Application service depends on domain interface
@Service
public class ExceptionProcessingService {
    private final ExceptionRepository repository; // Domain interface
    private final EventPublisher eventPublisher;   // Domain interface
    
    // Implementation details injected by Spring
}

// Infrastructure implements domain interfaces
@Repository
public class JpaExceptionRepository implements ExceptionRepository {
    // JPA-specific implementation
}
```

#### Error Handling

- **Use specific exceptions** for different error conditions
- **Handle exceptions at appropriate layers**
- **Log errors with context** (correlation IDs, transaction IDs)
- **Fail fast** for invalid inputs

```java
// Domain-specific exceptions
public class ExceptionNotFoundException extends RuntimeException {
    public ExceptionNotFoundException(String transactionId) {
        super("Exception not found with transaction ID: " + transactionId);
    }
}

// Service layer error handling
@Service
public class ExceptionQueryService {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionQueryService.class);
    
    public ExceptionDetailResponse getException(String transactionId) {
        try {
            InterfaceException exception = repository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ExceptionNotFoundException(transactionId));
            
            return mapper.toDetailResponse(exception);
        } catch (Exception e) {
            logger.error("Failed to retrieve exception with transactionId: {}", transactionId, e);
            throw new ExceptionProcessingException("Failed to retrieve exception", e);
        }
    }
}
```

## Testing Guidelines

### Testing Strategy

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **End-to-End Tests**: Test complete user workflows
- **Contract Tests**: Verify API contracts and event schemas

### Test Structure

Follow the **Arrange-Act-Assert** pattern:

```java
@Test
void shouldProcessExceptionSuccessfully() {
    // Arrange
    InterfaceException exception = TestDataBuilder.anException()
        .withTransactionId("tx-123")
        .withInterfaceType(InterfaceType.ORDER)
        .build();
    
    when(repository.save(any(InterfaceException.class))).thenReturn(exception);
    
    // Act
    service.processException(exception);
    
    // Assert
    verify(repository).save(exception);
    verify(eventPublisher).publishExceptionCaptured(exception);
    assertThat(exception.getStatus()).isEqualTo(ExceptionStatus.NEW);
}
```

### Test Naming

Use descriptive test names that explain the scenario:

```java
// Good
@Test
void shouldThrowExceptionWhenTransactionIdIsNull() { }

@Test
void shouldReturnEmptyListWhenNoExceptionsExist() { }

@Test
void shouldUpdateStatusToResolvedWhenRetrySucceeds() { }

// Bad
@Test
void testException() { }

@Test
void test1() { }
```

### Test Data

- **Use builders** for creating test data
- **Keep tests independent** - no shared mutable state
- **Use meaningful test data** that reflects real scenarios

```java
public class TestDataBuilder {
    public static InterfaceExceptionBuilder anException() {
        return new InterfaceExceptionBuilder()
            .withTransactionId(UUID.randomUUID().toString())
            .withInterfaceType(InterfaceType.ORDER)
            .withStatus(ExceptionStatus.NEW)
            .withTimestamp(Instant.now());
    }
}
```

### Coverage Requirements

- **Minimum 80% line coverage** for new code
- **100% coverage** for critical business logic
- **Focus on meaningful tests** over coverage metrics

## Documentation Standards

### Code Documentation

- **Public APIs**: Complete Javadoc with examples
- **Complex algorithms**: Inline comments explaining logic
- **Configuration**: Document all configuration properties

### API Documentation

- **OpenAPI/Swagger**: Keep API documentation up to date
- **Examples**: Provide request/response examples
- **Error codes**: Document all possible error responses

### Architecture Documentation

- **ADRs**: Document significant architectural decisions
- **Diagrams**: Use Mermaid for architecture diagrams
- **Runbooks**: Operational procedures and troubleshooting

## Pull Request Process

### Before Submitting

1. **Run all tests**: `./mvnw clean verify`
2. **Check code quality**: `./mvnw spotbugs:check pmd:check`
3. **Format code**: `./mvnw spotless:apply`
4. **Update documentation** if needed
5. **Rebase on main** to ensure clean history

### Pull Request Template

```markdown
## Description
Brief description of changes and motivation.

## Type of Change
- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] Tests pass locally
- [ ] No new warnings or errors

## Related Issues
Closes #123
```

### Review Process

1. **Automated checks** must pass (CI/CD pipeline)
2. **At least one approval** from a team member
3. **Address all feedback** before merging
4. **Squash commits** for clean history

## Issue Reporting

### Bug Reports

Include the following information:

- **Environment**: OS, Java version, deployment method
- **Steps to reproduce**: Clear, numbered steps
- **Expected behavior**: What should happen
- **Actual behavior**: What actually happens
- **Logs**: Relevant log entries with correlation IDs
- **Screenshots**: If applicable

### Feature Requests

Include the following information:

- **Problem statement**: What problem does this solve?
- **Proposed solution**: How should it work?
- **Alternatives considered**: Other approaches evaluated
- **Additional context**: Any other relevant information

### Security Issues

**Do not create public issues for security vulnerabilities.**

Instead:
1. Email security@biopro.com with details
2. Include "SECURITY" in the subject line
3. Provide detailed reproduction steps
4. Allow time for assessment and patching

## Development Tools

### Recommended IDE Setup

#### IntelliJ IDEA

1. **Install plugins**:
   - Lombok
   - SonarLint
   - CheckStyle-IDEA
   - SpotBugs

2. **Configure code style**:
   - Import `ide-config/intellij-code-style.xml`
   - Enable "Reformat code" on save
   - Enable "Optimize imports" on save

3. **Configure inspections**:
   - Enable all Java inspections
   - Configure custom inspection profiles

### Code Quality Tools

- **SpotBugs**: Static analysis for bug detection
- **PMD**: Source code analyzer for common programming flaws
- **Checkstyle**: Coding standard compliance
- **SonarQube**: Comprehensive code quality analysis

### Useful Commands

```bash
# Run all quality checks
./mvnw clean verify spotbugs:check pmd:check checkstyle:check

# Generate test coverage report
./mvnw clean test jacoco:report

# Format code according to standards
./mvnw spotless:apply

# Run integration tests
./mvnw verify -Pintegration-tests

# Build Docker image
docker build -t interface-exception-collector:dev .
```

## Getting Help

- **Documentation**: Check the README.md and wiki
- **Team Chat**: Use the #biopro-development Slack channel
- **Code Review**: Ask for help in pull request comments
- **Architecture Questions**: Schedule time with the tech lead

## Recognition

We appreciate all contributions! Contributors will be recognized in:

- Release notes for significant contributions
- Team meetings and retrospectives
- Annual team recognition events

Thank you for contributing to the Interface Exception Collector Service!