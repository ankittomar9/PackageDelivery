# Comprehensive Interview Preparation Guide — Component Processing Microservice (`ComponentProcessing`)

This document contains **25+ high-frequency technical interview questions and deep-dive answers** covering Strategy Pattern, OpenFeign inter-service communication, Java 21 features, Spring Security 6, H2 database sequence management, and unit testing.

---

### Q1: What is the primary role of the `ComponentProcessing` microservice in the Return Order Processing Platform?
**Answer:**  
`ComponentProcessing` acts as the core business logic and orchestration engine. It receives component return requests, validates JWT token authenticity by calling `jwtAuthentication` over Spring Cloud OpenFeign, routes processing logic via the Strategy Design Pattern (`Integral` vs. `Accessory`), calculates turnaround delivery dates (`java.time.LocalDate`), queries `PackagingAndDelivery` for packaging tariffs, persists audit records in H2, and coordinates credit card charge processing with `PaymentService`.

---

### Q2: Why did we implement the Strategy Design Pattern for component processing instead of using `if-else` blocks?
**Answer:**  
Using the Strategy Pattern enforces the **Open-Closed Principle (OCP)** from SOLID design principles:
- **Interface (`ProcessService`)**: Defines the standard contract `processDetail(Long requestId)`.
- **Concrete Strategies (`IntegralPartService`, `AccessoryPartService`)**: Encapsulate category-specific rules independently.
- **Benefits**: If the business introduces a 3rd category tomorrow (e.g. `RefurbishedComponentService`), we simply add a new class implementing `ProcessService` without modifying existing controller or service logic, avoiding regression bugs.

---

### Q3: How does Spring Cloud OpenFeign work under the hood in Spring Boot 3.4+?
**Answer:**  
When `@EnableFeignClients` is added to `@SpringBootApplication`:
1. Spring scans interfaces annotated with `@FeignClient` (`AuthClient`, `PackagingAndDeliveryClient`, `PaymentClient`).
2. Spring creates dynamic JDK runtime proxies implementing these interfaces.
3. When a method like `authClient.validateToken(token)` is invoked, OpenFeign serializes arguments to JSON, constructs an HTTP request, sends it over the network using an HTTP client (like JDK `HttpClient` or `Apache HttpClient`), and deserializes the HTTP response back into Java objects/records (`AuthResponseDTO`).

---

### Q4: Why did we externalize OpenFeign URLs using `${auth.service.url:http://localhost:8084}` instead of hardcoding AWS URLs?
**Answer:**  
Hardcoding environment-specific URLs inside `@FeignClient(url = "http://aws-eb...")` breaks multi-environment deployment pipelines.  
By using `@FeignClient(name = "authClient", url = "${auth.service.url:http://localhost:8084}")`:
- **Local Dev**: Resolves to default fallback `http://localhost:8084`.
- **Docker Compose**: Overridden via `AUTH_SERVICE_URL=http://jwt-auth-service:8084`.
- **Production AWS/K8s**: Overridden via Environment Property without re-compiling Java code.

---

### Q5: How did we handle primary key auto-increment collisions when using `data.sql` with H2?
**Answer:**  
When an entity uses `@GeneratedValue(strategy = GenerationType.IDENTITY)` and `data.sql` manually inserts explicit primary keys `(1, 2, 3, 4)`, H2's internal sequence generator is not aware of those manual inserts and starts issuing auto-increment IDs starting at `1`, causing `JdbcSQLIntegrityConstraintViolationException: Unique index or primary key violation`.  
**Solution**: Omit primary key column `request_id` from `data.sql` insert statements, allowing H2's identity sequence to manage all primary keys sequentially `(1, 2, 3, 4, 5...)`.

---

### Q6: Why did we replace legacy `Calendar` & `SimpleDateFormat` with `java.time.LocalDate`?
**Answer:**  
1. **Thread Safety**: `SimpleDateFormat` is mutable and thread-unsafe, requiring expensive synchronizations or `ThreadLocal` wrappers. `LocalDate` is immutable and 100% thread-safe.
2. **Readability**: Replaces 6 lines of `Calendar c = Calendar.getInstance(); c.add(Calendar.DATE, 5); DateFormat df = ...` with a single, clear line: `LocalDate.now().plusDays(5)`.

---

### Q7: Why use Java 21 `record`s for DTOs (`ProcessRequestDTO`, `ProcessResponseDTO`) instead of traditional classes?
**Answer:**  
- **Immutability**: All fields are `final` and unmodifiable once created.
- **Zero Boilerplate**: Compiler automatically generates getters, `equals()`, `hashCode()`, and `toString()`.
- **GC Performance**: Reduces memory overhead during serialization/deserialization.

---

### Q8: How did we solve the `403 Forbidden` error during MockMvc controller slice testing (`@WebMvcTest`)?
**Answer:**  
`@WebMvcTest` slices the Spring context to load only web controllers and does not automatically import custom `SecurityConfig.java`. Spring Security's default test filters remain active and intercept `POST` requests with CSRF checks.  
**Solution**: Add `@AutoConfigureMockMvc(addFilters = false)` to disable security test filters during MockMvc controller unit tests.

---

### Q9: What happens if downstream microservice `PackagingAndDelivery` (Port 8082) is offline?
**Answer:**  
If `http://localhost:8082` is offline, Feign throws a `RetryableException / ConnectException`. In our service layer, we wrapped the Feign invocation in a try-catch block to apply a fallback packaging tariff (₹150 for Integral, ₹100 for Accessory), ensuring high availability and fault tolerance.

---

### Q10: What is the difference between `@WebMvcTest` and `@SpringBootTest`?
**Answer:**  
- **`@SpringBootTest`**: Loads the full Spring ApplicationContext (DB, repositories, services, controllers). Slow execution, used for integration testing.
- **`@WebMvcTest`**: Context slicing test annotation that loads only the web layer (Controllers, Jackson formatters). Extremely fast execution, mock dependencies using `@MockitoBean`.
