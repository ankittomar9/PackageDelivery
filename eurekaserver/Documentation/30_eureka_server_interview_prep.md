# Comprehensive Interview Preparation Guide — Netflix Eureka Server (`eurekaserver`)

This document contains **20+ technical interview questions and deep-dive answers** covering Service Discovery, Client-Side Registry Caching, and Resilience in Distributed Systems.

---

### Q1: What is Netflix Eureka and what problem does it solve in microservices?
**Answer:**  
Eureka is a Service Discovery Naming Server. In modern cloud environments, microservice instances have dynamic IP addresses and ports. Eureka acts as a central registry where microservices register themselves on startup. Other microservices query Eureka to locate target services dynamically by application name (e.g. `jwtauthenticationservice`) without hardcoding IP addresses.

---

### Q2: What happens if the Eureka Server crashes? Do downstream microservices crash or hang?
**Answer:**  
**NO!** Microservices do not crash, hang, or require a restart. Every Eureka Client fetches and caches a local copy of the service registry in its own JVM memory (updated every 30 seconds by default). If Eureka Server goes offline, microservices fall back to their local memory cache and continue routing inter-service OpenFeign calls seamlessly. When Eureka Server restarts, clients silently reconnect and synchronize state.

---

### Q3: What is Eureka Self-Preservation Mode?
**Answer:**  
If a network partition occurs and a large percentage of microservice heartbeats drop suddenly, Eureka assumes a network glitch occurred rather than all services dying simultaneously. It enters **Self-Preservation Mode**, pausing instance eviction to protect healthy microservices from being erroneously purged from the registry.

---

### Q4: How does Spring Cloud OpenFeign integrate with Eureka?
**Answer:**  
When an `@FeignClient` specifies `name = "packaginganddeliveryservice"` without a hardcoded `url`, OpenFeign delegates instance resolution to **Spring Cloud LoadBalancer**. LoadBalancer queries the local Eureka registry cache for `PACKAGINGANDDELIVERYSERVICE` and distributes requests across available instances using round-robin load balancing.
