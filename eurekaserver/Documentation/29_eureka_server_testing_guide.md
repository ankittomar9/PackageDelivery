# Comprehensive Testing Guide — Netflix Eureka Naming Server (`eurekaserver`)

This guide outlines verification procedures for **`eurekaserver`** and outage resilience testing.

---

## 1. Eureka Dashboard Verification

1. Start `EurekaServerApplication` (Port 8761).
2. Open browser at **`http://localhost:8761`**.
3. Verify that under **"Instances currently registered with Eureka"**, the following 4 services appear with status `UP (1)`:
   - `COMPONENTPROCESSINGSERVICE` (Port 8081)
   - `JWTAUTHENTICATIONSERVICE` (Port 8084)
   - `PACKAGINGANDDELIVERYSERVICE` (Port 8082)
   - `PAYMENTSERVICE` (Port 8083)

---

## 2. Outage Resilience & Client Caching Test Procedure

1. Start all 4 microservices and `eurekaserver`.
2. Stop `EurekaServerApplication` (Port 8761).
3. Submit a return request on `webportal` (`http://localhost:5173`).
4. **Expected Result**: The request succeeds with **HTTP 200 OK**! `ComponentProcessing` uses its in-memory local registry cache to route calls to `PackagingAndDelivery` without crashing or hanging.
5. Restart `EurekaServerApplication`. Microservices automatically reconnect silently in the background.
