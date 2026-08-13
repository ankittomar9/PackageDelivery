# Comprehensive Interview Preparation Guide — Packaging & Delivery Microservice (`PackagingAndDelivery`)

This document contains **20+ technical interview questions and deep-dive answers** covering thread-safety refactoring, Domain-Driven Design (DDD), Spring Security 6, and REST error handling.

---

### Q1: What is the main purpose of the `PackagingAndDelivery` microservice?
**Answer:**  
`PackagingAndDelivery` is a lightweight, high-performance rule engine that calculates packaging material costs, protective handling charges (ESD/bubble wrap), and freight carrier tariffs based on component categories (`Integral` vs `Accessory`) and defective quantity counts. It is invoked synchronously by `ComponentProcessing` over OpenFeign.

---

### Q2: What severe concurrency bug existed in the legacy `PackagingAndDeliveryService` code, and how was it resolved?
**Answer:**  
- **Legacy Bug**: The legacy service declared `private int packagingAndDeliveryCost = 0;` as an instance field on a Spring `@Service` singleton bean. Under high concurrent web traffic, multiple threads mutated this instance field simultaneously, overwriting each other's calculations and returning wrong tariffs to users.
- **Resolution**: Refactored the service to be **100% stateless**. All calculations are performed within method-scoped local variables (`double itemCost`, `double totalCharge`), guaranteeing absolute thread safety across high-concurrency requests.

---

### Q3: Why is `PackagingAndDelivery` decoupled as a separate microservice rather than kept as a utility class inside `ComponentProcessing`?
**Answer:**  
1. **Domain Bounded Context (DDD)**: Order return processing (RMA policies, warranty SLAs) belongs to the Order Processing Domain, whereas carrier tariffs and packaging materials belong to the Logistics Domain.
2. **Independent Deployability**: Freight tariffs, shipping vendor contracts (FedEx/DHL rates), and material costs change frequently. Decoupling this logic allows logistics teams to update tariff tables and re-deploy `PackagingAndDelivery` without risk or downtime for core order processing.

---

### Q4: Why did we change the exception handler for `ComponentTypeNotFoundException` from HTTP 204 (No Content) to HTTP 404 (Not Found)?
**Answer:**  
HTTP 204 (No Content) indicates successful request execution with an empty response body. Returning 204 for an invalid component error causes client frameworks (and OpenFeign) to interpret the call as a successful response, leading to silent calculation bugs. Returning **HTTP 404 (Not Found)** with an `ErrorResponseDTO` clearly informs the caller that the requested resource/category does not exist.

---

### Q5: How does `PackagingAndDeliveryController` enforce case-insensitivity for path variables?
**Answer:**  
Inside `PackagingAndDeliveryService`, string comparisons use `"integral".equalsIgnoreCase(componentType)` and `"accessory".equalsIgnoreCase(componentType)`. This ensures path parameters like `/Integral/1`, `/integral/1`, or `/INTEGRAL/1` execute without error.
