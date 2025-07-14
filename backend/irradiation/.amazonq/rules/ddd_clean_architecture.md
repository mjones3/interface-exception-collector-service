# DDD and Clean Architecture Standards

## Overview
This document defines the architectural standards for implementing Domain-Driven Design (DDD) with Clean Architecture using Spring Boot WebFlux, GraphQL, and R2DBC.

---

## 1. Domain Layer

### Core Principles
- **Framework-agnostic**: Domain never imports Spring Framework classes
- **Business-focused**: Contains only business logic and rules
- **Self-contained**: All domain concepts are defined within this layer

### Aggregate Composition Rules (CRITICAL)
- **Within the same aggregate boundary**: Use full domain entities and value objects
    - Example: `OrderAggregate` contains full `OrderItem`, `ShippingAddress`, `PaymentMethod` entities
    - This maintains consistency and allows the aggregate to enforce business invariants
- **Cross-aggregate references**: Use only IDs, never full entities from other aggregates
    - Example: Reference `customerId` (Long) instead of full `Customer` entity from another bounded context
    - This prevents tight coupling between aggregates and maintains clear boundaries
- **Aggregate Root Responsibility**: The aggregate root manages the entire aggregate lifecycle
    - All business rules and invariants are enforced within the aggregate boundary
    - External access to aggregate internals must go through the aggregate root
- **Transaction Boundaries**: One transaction per aggregate - transaction boundaries align with aggregate boundaries

### Aggregates and Entities
- **One root aggregate per subdomain**
- **Aggregates MUST be classes** (stateful, not records)
- **Domain entities MUST be classes** (stateful, not records)

### Value Objects vs Domain Services (CRITICAL)

#### Value Object Responsibility:
- **MUST be records** with validation in constructors
- **Self-validating**: Value objects ensure they're always in a valid state once created
- **Encapsulation**: All business rules related to the value object belong inside it
- **Immutability**: Once created, value objects cannot be changed
- **Throw BusinessRequestException** for validation failures
- **Use for format validation**: Email, phone, unit numbers, etc.

#### When NOT to Create Domain Services:
- **NEVER create services for simple validation** that belongs to a value object
- **NEVER create services for format validation** (e.g., email format, phone format, unit number format)
- **NEVER create services for single-value validation** that can be handled in value object constructor

#### When to Create Domain Services:
- **Cross-aggregate business logic** that doesn't belong to a single aggregate
- **Complex calculations** involving multiple domain objects
- **Business processes** that coordinate multiple aggregates
- **Domain logic** that requires external dependencies (repositories, external services)

### Domain Services (CRITICAL)
- **Framework-agnostic**: Domain service implementations MUST NOT use Spring annotations
- **Repository injection**: Domain services CAN inject domain repository interfaces (valid DDD pattern)
- **External dependencies**: Domain services requiring repositories or external services are valid
- **Service Configuration**: Create configuration class in `infrastructure.config.service` package and add bean for domain service injection
- **Bean creation**: Use `@Bean` methods with constructor injection for domain services
- **Example**:
```java
// ✅ CORRECT: Domain service implementation (no Spring annotations)
public class BatchNumberGenerationServiceImpl implements BatchNumberGenerationService {
    private final AntigenBatchRepository repository; // Valid domain repository injection
}

// ✅ CORRECT: Infrastructure configuration
@Configuration
public class ServiceConfiguration {
    @Bean
    public BatchNumberGenerationService batchNumberGenerationService(AntigenBatchRepository repo) {
        return new BatchNumberGenerationServiceImpl(repo);
    }
}
```

### Repository Interfaces
- **Define in**: `domain.{subdomain}.repository` package
- **Return reactive types**: `Mono<T>` and `Flux<T>`
- **Implementation in infrastructure layer**

### Domain Events
- **Use for cross-aggregate communication**
- **Past tense naming**: `OrderCreated`, `BatchCompleted`
- **Immutable records**: Domain events should be records
- **Publish from aggregate roots**: Events represent state changes

### Domain Enums
- **Package**: Place in `domain.{subdomain}.enums` package
- **Business concepts**: Enums represent domain concepts and business rules
- **Business logic**: Should contain business logic methods when appropriate
- **Immutable**: Enums are naturally immutable domain concepts

### Domain Invariants
- **Validate in constructors**: Use constructor validation for required invariants
- **Aggregate enforcement**: Validate business rules within aggregates and entities
- **Exception handling**: Throw domain exceptions for business rule violations
- **State consistency**: Ensure domain objects are always in valid state

### Exception Handling
- **Domain exceptions extend GenericException**
- **Correct import**: `domain.exception.GenericException`
- **Use Spring Assert with try-catch wrapping**:
```java
try {
    Assert.hasText(value, "Value cannot be empty");
} catch (IllegalArgumentException e) {
    throw new BusinessRequestException(e.getMessage());
    }
```

### Package Structure
```
domain/
└── {subdomain}/
    ├── aggregate/
    ├── entity/
    ├── valueobject/
    ├── repository/
    ├── service/
    ├── exception/
    └── enums/
```

---

## 2. Application Layer

### Use Cases
- **Interface + Implementation pattern**
- **Use @UseCase annotation** on implementations only
- **Constructor injection** for all dependencies
- **CQRS Compliance**: Strictly separate commands (write) from queries (read)
    - **Queries**: Return data, no state modification, idempotent
    - **Commands**: Modify state, may return confirmation/result

### Use Case Interface Selection (MANDATORY)
- Every use case should extend one of the below marker interfaces
- Inject the use case concrete class using the constructor

#### Interface Selection Rules:
- **No-input queries**: Use `QueryUseCase<O>` → `Mono<O> execute()`
- **No-input collections**: Use `FluxQueryUseCase<O>` → `Flux<O> execute()`
- **Parameterized queries**: Use `ParameterizedQueryUseCase<I, O>` → `Mono<O> execute(I input)`
- **Parameterized collections**: Use `ParameterizedFluxQueryUseCase<I, O>` → `Flux<O> execute(I input)`
- **Write operations**: Use `CommandUseCase<I, O>` → `Mono<O> execute(I command)`

### Command Object Guidelines
- **Create Command objects when**:
    - Use case requires 3+ parameters
    - Parameters form a cohesive business concept
    - Future extensibility is likely needed

- **Use primitive/simple types when**:
    - Single parameter use cases (e.g., `findById(Long id)`)
    - Simple queries with one filter criteria
    - No business logic validation needed on the parameter

- **Examples**:
    - ✅ `CreateOrderCommand(customerId, items, shippingAddress)` - Multiple related parameters
    - ✅ `ValidateUnitUseCase extends ParameterizedQueryUseCase<String, ValidationResult>` - Single parameter
    - ❌ `FindByIdCommand(Long id)` - Unnecessary wrapper for single ID

### DTOs and Commands
- **MUST be records** with `@Builder`

### Use Case Implementation Standards (MANDATORY)

#### Mapping Requirements (CRITICAL):
- **NEVER use manual mapping** in use case implementations
- **NEVER create DTOs manually** using builders in use cases
- **ALWAYS inject and use MapStruct mappers** for domain ↔ DTO conversion
- **Create application mappers** for each domain entity that needs DTO conversion
- **Constructor injection** for all mappers in use case implementations
- **Separation of Concerns**: Keep mapping logic separate from business logic
- **All DTO creation MUST go through mappers**: Use cases should never instantiate DTOs directly

### Error Handling
- **Use `onErrorMap()`** for reactive error transformation
- **Convert domain exceptions** to application-appropriate responses

### Package Structure
```
application/
└── {subdomain}/
    ├── usecase/
    ├── service/
    ├── dto/
    ├── command/
    └── mapper/
```

---

## 3. Infrastructure Layer

### WebFlux Database Constraints
- **DB entities cannot use JPA** as we are using WebFlux
- **DB repositories must implement ReactiveCrudRepository**

### Database Entity Standards (CRITICAL)
- **CRITICAL: DB entities MUST be classes, NOT records** - Records are immutable and incompatible with Spring Data R2DBC updates
- **Use Lombok**: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **ID Types**: LK tables use `Persistable<Integer>`, BLD tables use `Persistable<Long>`
- **Required Interfaces**: All entities must implement `Serializable, Persistable<ID_TYPE>`

### Entity Repository Standards
- **Extend**: `ReactiveCrudRepository`
- **Annotation**: `@Repository` stereotype (enables Spring's exception translation and component scanning)
- **Package Access**: DB Entities, entity repositories, and entity mappers should have package-level access

### Repository Implementation Standards (MANDATORY)
- **EVERY domain repository interface MUST have a corresponding implementation**
- **Implementation naming**: `{DomainRepository}Impl` (e.g., `UserRepositoryImpl`)
- **Location**: `infrastructure.persistence.{subdomain}` package
- **Annotation**: MUST use `@DomainRepository` annotation
- **Method Signatures**: Ensure exact match between domain interface and implementation
- **Entity Repository Support**: Create corresponding entity repository methods for each domain repository method
- **Missing Methods**: Use `UnsupportedOperationException` for methods that can't be implemented immediately

#### Verification Process (CRITICAL):
1. **List all domain repository interfaces** in `domain.{subdomain}.repository` package
2. **Check corresponding implementations** exist in `infrastructure.persistence.{subdomain}` package
3. **Verify method signatures match exactly** between interface and implementation
4. **Use placeholder implementations** with `UnsupportedOperationException` if not ready

### Package Structure
```
infrastructure/
├── persistence/
│   └── {subdomain}/
│       ├── {Entity}.java
│       ├── {Entity}Repository.java
│       ├── {Entity}Mapper.java
│       └── {DomainRepository}Impl.java
└── config/
    └── service/
```
#### Package Structure Rules
- **CRITICAL: DB repositories, DB entities, DB mappers, and their implementations MUST be placed in the SAME package/directory structure** - never separate them into different packages as this breaks package-level access control
- All persistence components (entities, entity repositories, domain repository implementations) should be co-located within the same subdomain package under persistence

---

## 4. Adapter Layer
- Annotate controllers with the `@Controller` annotation and use GraphQL controller standards
- Add the controllers inside `adapter.in.web.{subdomain}` package
- DTOs, Mappers should have their own package inside each subdomain
- Named the queries and controllers with a meaningful name `{verb}{object}`
- The request objects must be DTOs instead of inputs, and they should have a mapper to convert to its respective command

### GraphQL Annotations
- Use `@QueryMapping` for GraphQL queries
- Use `@MutationMapping` for GraphQL mutations
- Use `@Argument` for GraphQL arguments

### GraphQL Controller Standards

#### Return Types:
- **Single Objects**: Use `Mono<T>` for single results
- **Collections**: Use `Flux<T>` directly, NEVER `Mono<Flux<T>>`
- **Void Operations**: Use `Mono<Void>`

#### Argument Mapping:
- **@Argument**: Must match GraphQL schema parameter names exactly
- **Input Objects**: Use `@Argument("inputName")` where inputName matches schema
- **Primitive Arguments**: Use `@Argument` without name if parameter names match

### Package Structure
```
adapter/
└── in/
    └── web/
        └── {subdomain}/
            ├── dto/
            ├── mapper/
            └── {Controller}.java
```

---

## 5. Testing Strategy

- **Use**: `@ExtendWith(MockitoExtension.class)`
- **Mock all dependencies** with `@Mock`
- **Use** `@GraphQlTest` for GraphQL testing
- **Use StepVerifier** for reactive testing

### Unit vs Integration Testing Standards (CRITICAL)

#### Test Classification Rules:
- **Unit Tests**: Test single class in isolation with mocked dependencies
- **Integration Tests**: Test multiple components together with real dependencies
- **AVOID integration tests** unless specifically testing component integration

### GraphQL Testing Standards (MANDATORY)

#### @GraphQlTest Configuration:
- **ALWAYS import GraphQLConfig AND its dependencies** when using custom scalars
- **Include ALL configuration dependencies** (CustomMessageInterpolator, etc.)
- **Use @Import annotation** to include necessary configuration classes
- **Mock dependencies**: `@MockitoBean` (NOT deprecated `@MockBean`)

#### GraphQL Test Documents:
- **ALWAYS create separate .graphql files** in `src/test/resources/graphql-test/`
- **File naming**: Match query/mutation name with `.graphql` extension
- **Use `documentName()` instead of `document()`** for better maintainability

### Integration Tests
- **Avoid unless necessary** for component integration testing
- **Use**: `@SpringBootTest` only when testing real dependencies

---

## 6. Code Standards

### Entity Types
- **Records (Immutable)**: Value objects, DTOs, commands
- **Classes (Mutable)**: Domain entities, aggregates, database entities

### Lombok Usage
- **Mandatory Lombok Usage**: Use Lombok annotations extensively across all layers to reduce boilerplate code, but not the experimental annotations
    - Use `@Getter` and `@Setter` for field access instead of manual getter/setter methods
    - Use `@Builder` for all domain entities, value objects, DTOs, commands, and database entities
    - Use `@Value` for immutable classes when not using records
    - Use `@NoArgsConstructor`, `@AllArgsConstructor` when needed for frameworks
    - Use `@ToString`, `@EqualsAndHashCode` when appropriate
- **Custom Methods**: Only write manual getters/setters when custom logic is required
    - Example: `getUnits()` returning defensive copy `new ArrayList<>(units)`
    - Example: Custom validation in setters for aggregates
- **Framework Compatibility**: Lombok annotations work seamlessly with Spring Framework and other libraries
- **Avoid Manual Boilerplate**: Never write manual getters, setters, constructors, toString, equals, or hashCode when Lombok can generate them

### Reactive Programming (CRITICAL)
- **Use**: `Mono<T>` and `Flux<T>` consistently
- **Prefer**: `flatMap()` over blocking operations
- **NEVER use blocking calls**: `.block()`, `.subscribe()` in production code
- **Domain services MUST be fully reactive**: All domain services must return `Mono<T>` or `Flux<T>`
- **Repository implementations MUST be reactive**: No blocking database calls allowed

### Class Design Principles
- **Composition over inheritance**: Prefer small, focused classes over subclassing
- **No unused code**: Remove unused methods, classes, or exceptions
- **Single responsibility**: Each class should have one clear purpose

### Collection Types
- **Use specific collection types**: `List<T>`, `Set<T>`, `Map<K,V>` instead of `Iterable<T>`
- **Prefer immutable collections**: Use `List.of()`, `Set.of()`, `Map.of()` for static data
- **Clear semantics**: Choose collection type based on business intent (ordered vs unordered, duplicates vs unique)

### MapStruct Mapping Standards

#### Entity Mappers:
- **Audit Fields**: Use `@Mapping(target = "auditField", ignore = true)` for createDate, modificationDate when mapping domain → entity
- **Missing Fields**: Use `@Mapping(target = "field", ignore = true)` when target has fields not in source
- **Field Name Differences**: Use `@Mapping(source = "sourceField", target = "targetField")` for different field names
- **Complex Objects**: Use `@Mapping(target = ".", source = "nestedObject")` for flattening

#### Spring Integration:
- **ALL mappers MUST use**: `@Mapper(componentModel = "spring")`
- This enables Spring dependency injection for mappers
- Without this configuration, mappers cannot be injected into use cases or controllers

---

## 7. Naming Conventions

- **Aggregates**: Use business domain names (e.g., Order, Customer)
- **Value Objects**: Descriptive names ending with purpose (e.g., EmailAddress, Money)
- **Use Cases**: Action-oriented names (e.g., CreateOrderUseCase, FindCustomerQuery)
- **Domain Events**: Past tense (e.g., OrderCreated, CustomerRegistered)
- **Commands**: Imperative verbs (e.g., CreateOrderCommand, UpdateCustomerCommand)

---

## 8. Architectural Constraints

### Layer Dependencies
- **Domain**: Only Java standard library + domain classes
- **Application**: Domain + MapStruct + Reactor
- **Infrastructure**: All layers + Spring Framework
- **Adapter**: All layers + GraphQL + Spring Web

### CQRS Pattern
- **Commands**: Modify state, return confirmation
- **Queries**: Read data, no side effects
- **Separate interfaces** for different operation types

### Error Handling Strategy
- **Domain**: Spring Assert + domain exceptions
- **Application**: Reactive error mapping (`onErrorMap()`)
- **Adapter**: GraphQL error resolver for clean client responses

---

## 9. Quality Assurance

### ArchUnit Integration

Use [ArchUnit](https://www.archunit.org/) tests to verify that:

Verify architectural constraints:
- Domain layer independence
- No circular dependencies
- Proper layer boundaries
- Aggregate reference rules

### Code Review Checklist
- [ ] Correct use case interface selection (Query vs Command)
- [ ] CQRS compliance (commands modify state, queries don't)
- [ ] Proper entity type (record vs class)
- [ ] MapStruct mappers with Spring integration
- [ ] Domain exceptions with correct imports
- [ ] Package structure compliance
- [ ] Unit tests with proper mocking
- [ ] Specific collection types used (List, Set) instead of Iterable
- [ ] Composition over inheritance (no unnecessary subclasses)
- [ ] No unused code (methods, classes, exceptions)
- [ ] Aggregate reference rules (IDs for cross-aggregate, full entities within aggregate)
- [ ] Repository implementations match domain interface contracts
- [ ] Domain services configured as Spring beans in infrastructure layer
- [ ] No Spring annotations in domain service implementations

---

## 10. Common Anti-Patterns to Avoid

- ❌ Using `Void` parameters in use case interfaces
- ❌ Manual mapping in use case implementations
- ❌ Manual DTO creation using builders in use cases
- ❌ Blocking calls (.block(), .subscribe()) in production code
- ❌ Records for database entities
- ❌ Records for domain entities
- ❌ Domain services for simple validation
- ❌ Integration tests instead of unit tests
- ❌ `@MockBean` (deprecated) instead of `@MockitoBean`
- ❌ Inline GraphQL documents instead of external files
- ❌ Cross-aggregate entity references (use IDs only, never full entities)
- ❌ Framework dependencies in domain layer
- ❌ Spring annotations in domain service implementations
- ❌ Missing Spring configuration for domain services
- ❌ Using `Iterable<T>` instead of specific collection types (`List<T>`, `Set<T>`)
- ❌ Mutable collections in public APIs without defensive copying
- ❌ Creating subclasses instead of composition (prefer small, focused classes)
- ❌ Unused methods, classes, or exceptions in codebase
- ❌ Incomplete repository implementations (missing domain interface methods)

