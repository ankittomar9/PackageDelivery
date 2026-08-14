# High-Level Design (HLD) — Netflix Eureka Naming Server (`eurekaserver`)

## 1. System Overview & Service Discovery Architecture

The **Eureka Naming Server** (`eurekaserver`, running on port **8761**) is the dynamic service discovery registry of the Return Order Processing Platform. 

It eliminates hardcoded IP addresses and ports across microservices by acting as a central **phonebook/directory**. Every microservice (`jwtAuthentication`, `ComponentProcessing`, `PackagingAndDelivery`, `PaymentService`) registers its network location with Eureka on startup and sends periodic heartbeats.

```
                                  +------------------------------------+
                                  |   Netflix Eureka Server (Port 8761)|
                                  +------------------------------------+
                                    ^               ^               ^
               1. Register &        |               |               | 3. Fetch Local
                  Heartbeats (30s)  |               | 2. Dynamic    |    Registry Cache
                                    |               |    Lookup     |
         +--------------------------+               |               +--------------------------+
         |                                          |                                          |
+---------------------+                    +---------------------+                    +---------------------+
| PackagingAndDelivery|                    | ComponentProcessing |                    | PaymentService      |
|     (Port 8082)     |                    |     (Port 8081)     |                    |     (Port 8083)     |
+---------------------+                    +---------------------+                    +---------------------+
```

---

## 2. Key Responsibilities & Fault-Tolerance Features

1. **Dynamic IP & Port Registry**: Allows microservices to scale horizontally across Docker/Kubernetes clusters without code updates.
2. **Client-Side Memory Caching**:
   - **What happens if Eureka Server crashes?**
   - Microservices do **NOT** crash, hang, or require a restart!
   - Every Eureka Client caches a local copy of the registry in its own JVM memory (updated every 30 seconds). If Eureka goes down, clients use their local cache to route OpenFeign calls seamlessly.
3. **Eureka Self-Preservation Mode**: If network partition issues occur and heartbeats drop suddenly, Eureka enters self-preservation mode to prevent removing healthy instances prematurely.
4. **Automatic Background Re-connection**: When Eureka Server restarts, clients silently reconnect and synchronize their registry state without downtime.

---

## 3. Registered Instances Matrix

| Microservice Application Name | Port | Discovery Role | Health Check |
| :--- | :--- | :--- | :--- |
| `JWTAUTHENTICATIONSERVICE` | 8084 | Eureka Client | `/actuator/health` |
| `COMPONENTPROCESSINGSERVICE` | 8081 | Eureka Client + Feign Consumer | `/actuator/health` |
| `PACKAGINGANDDELIVERYSERVICE` | 8082 | Eureka Client | `/actuator/health` |
| `PAYMENTSERVICE` | 8083 | Eureka Client | `/actuator/health` |
