# Domain Implementation Phases

## Phase 1: Database Schema and Migration Scripts
**Goal**: Create all database tables and initial data

### Pre-execution:
- Execute `powershell "Get-Date -Format 'yyyy.MM.dd'"` command to get current date in correct format (YYYY.MM.DD) for migration file naming

### Tasks:
- Create database migration script following naming conventions (date will be auto-calculated)
- Create all LK (lookup) tables with proper constraints and indexes
- Create all BLD (business logic data) tables with proper relationships
- Insert initial configuration and lookup data
- Add foreign keys, unique constraints, and indexes as needed

**Note**: Migration file name will be automatically generated using current date and next sequence number

## Phase 2: Domain Layer Implementation
**Goal**: Create domain entities, value objects, and business logic

### Tasks:
- Create domain aggregates and entities following DDD principles
- Create value objects with proper validation logic
- Create domain enums representing business concepts
- Create domain services for complex business logic
- Create repository interfaces in domain layer
- Ensure domain layer is framework-agnostic

## Phase 3: Infrastructure Layer - Persistence
**Goal**: Implement database entities, repositories, and mappers

### Tasks:
- Create database entities implementing Serializable and Persistable interfaces
- Create entity repositories extending ReactiveCrudRepository
- Create entity mappers using MapStruct for domain ↔ entity conversion
- Create domain repository implementations with @DomainRepository annotation
- Ensure all persistence components are co-located in same package
- Use package-level access for entities and entity repositories

## Phase 4: Application Layer - Use Cases
**Goal**: Implement business use cases and application services

### Tasks:
- Create command objects for write operations (use records with @Builder)
- Create query objects for read operations
- Create use cases extending CommandUseCase interface with @UseCase annotation
- Create query use cases extending QueryUseCase interface
- Create DTOs for data transfer between layers
- Create MapStruct mappers between DTOs and commands/queries
- Follow CQRS pattern separating commands from queries

## Phase 5: Adapter Layer - GraphQL API
**Goal**: Create GraphQL mutations and queries for the web interface

### Tasks:
- Create GraphQL schema with mutations and queries
- Create GraphQL controllers using @MutationMapping and @QueryMapping
- Create request DTOs for GraphQL operations
- Create MapStruct mappers between GraphQL DTOs and application layer
- Follow meaningful naming conventions for queries and mutations
- Place controllers in adapter.in.web.{subdomain} package

## Phase 6: Configuration and Service Wiring
**Goal**: Configure Spring beans and dependency injection

### Tasks:
- Create service configuration classes in infrastructure.config.service package
- Configure domain services as Spring beans
- Wire use case implementations with constructor injection
- Configure repository implementations
- Set up validation configurations as needed

## Phase 7: Testing Implementation
**Goal**: Create comprehensive test coverage

### Tasks:
- Create unit tests for domain entities and services
- Create unit tests for use cases using Mockito
- Create integration tests for repository implementations
- Create GraphQL endpoint tests using @GraphQlTest
- Create test data builders and mock factories
- Use StepVerifier for reactive testing patterns

## Phase 8: Error Handling and Validation
**Goal**: Implement proper error handling and validation

### Tasks:
- Create domain-specific exceptions extending GenericException
- Create validation services for business rules
- Use Spring Assert utility for domain validation with try-catch wrapping
- Create error mappers for GraphQL error responses
- Implement reactive error handling with onErrorMap() and onErrorReturn()
- Ensure consistent error handling across all layers

## Update Phases (For Existing Components)

### Update Phase 1: Database Schema Changes
**Goal**: Update existing database tables and related components

#### Pre-execution:
- Execute `powershell "Get-Date -Format 'yyyy.MM.dd'"` command to get current date
- Identify the specific table(s) being modified

#### Tasks:
- Create ALTER TABLE migration script with proper naming
- Update affected database entity classes
- Update entity mappers (MapStruct) for new fields
- Update domain repository implementations if needed
- Maintain backward compatibility

### Update Phase 2: Domain Entity Changes
**Goal**: Update domain entities and related domain components

#### Tasks:
- Update domain entity/aggregate classes with new fields
- Update value objects if field changes affect validation
- Update domain services if business logic changes
- Update repository interfaces if new query methods needed
- Ensure domain layer remains framework-agnostic

### Update Phase 3: Application Layer Changes
**Goal**: Update use cases, DTOs, and application services

#### Tasks:
- Update DTOs and command objects with new fields
- Update MapStruct mappers between domain and application layers
- Update affected use case implementations
- Update query/command interfaces if signatures change
- Maintain CQRS compliance

### Update Phase 4: Adapter Layer Changes
**Goal**: Update GraphQL schema and controllers

#### Tasks:
- Update GraphQL schema with new fields
- Update GraphQL DTOs and request objects
- Update controller methods if needed
- Update MapStruct mappers between adapter and application layers
- Update GraphQL test documents

### Update Phase 5: Testing Updates
**Goal**: Update tests for modified components

#### Tasks:
- Update unit tests for modified domain entities
- Update use case tests with new field scenarios
- Update GraphQL endpoint tests
- Update test data builders and mock factories
- Add tests for new validation rules or business logic

## Execution Instructions

### For New Development:
- Based on the business requirements in {your_md_file}, execute Phase 3 from @dev_phases.md following the architectural standards in @.amazonq/rules/
- Based on ```business requirements```, execute Phase 1 from @dev_phases.md following the architectural standards in @.amazonq/rules/

### For Updates/Changes:
- "Update the {entity_name} entity to add {field_name} field, execute Update Phase 1 from @dev_phases.md"
- "Add new business logic to {aggregate_name}, execute Update Phase 2 from @dev_phases.md"
- "Update GraphQL schema for {entity_name} changes, execute Update Phase 4 from @dev_phases.md"

**Note:** If the Rules are preselected in the prompt then you don't need to mention them

## Notes

- Complete each phase before moving to the next
- Follow DDD and Clean Architecture principles
- Use reactive programming patterns throughout
- Maintain proper aggregate boundaries and transaction scopes
