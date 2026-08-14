# Detailed Code Walkthrough — Netflix Eureka Naming Server (`eurekaserver`)

This document provides a line-by-line breakdown of **`EurekaServerApplication.java`** and Eureka client registration mechanisms.

---

## 1. `EurekaServerApplication.java`

```java
package com.company.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### 💡 Line-by-Line Explanation
- **Line 7 (`@EnableEurekaServer`)**: Activates Spring Cloud's embedded Netflix Eureka Registry, mounting the Eureka REST registry endpoints and web dashboard UI at `/`.

---

## 2. Dynamic OpenFeign Resolution Mechanism (`AuthClient.java`)

```java
@FeignClient(name = "jwtauthenticationservice")
public interface AuthClient {
    @GetMapping("/validate")
    AuthResponseDTO validateToken(@RequestHeader("Authorization") String tokenHeader);
}
```

### 💡 How Dynamic Resolution Works
1. Omitting `url = "http://..."` signals Spring Cloud OpenFeign to consult the **Eureka Discovery Client**.
2. OpenFeign queries the local Eureka registry cache for application name `JWTAUTHENTICATIONSERVICE`.
3. Spring Cloud LoadBalancer selects an active instance (e.g. `http://192.168.1.50:8084`) and executes the HTTP request.
