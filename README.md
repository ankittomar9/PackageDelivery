# 🚚 PackageDelivery - Enterprise Return Order Management Platform

[![Java Version](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.2.8-blue)](https://react.dev/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://www.docker.com/)
[![Microservices](https://img.shields.io/badge/Architecture-Microservices-red)](https://microservices.io/patterns/microservices.html)

A production-grade, microservices-based platform for managing defective component returns with integrated authentication, payment processing, and real-time notifications. Built with modern Spring Boot 3.4.2, Java 21, and React 19.

## 🌟 Features

### Core Business Capabilities
- **Defective Component Return Processing**: Streamlined workflow for handling integral and accessory component returns
- **Dynamic Pricing Calculation**: Automatic computation of processing charges, packaging fees, and delivery costs
- **Priority Request Handling**: Expedited processing with premium pricing for urgent returns
- **Multi-Step Workflow**: Form submission → Cost review → Payment → Digital receipt generation

### Enterprise-Grade Features
- **Microservices Architecture**: 10 loosely coupled services with independent scalability
- **Centralized Authentication**: JWT-based security with token validation across all services
- **Service Discovery**: Netflix Eureka for dynamic service registration and load balancing
- **API Gateway**: Single entry point with intelligent routing and CORS configuration
- **Event-Driven Communication**: Kafka-based asynchronous messaging for payment notifications
- **Payment Integration**: Stripe gateway integration with fallback inter-service payment processing
- **Real-time Notifications**: Event-driven SMS and email dispatch system
- **Comprehensive Audit Trail**: Complete request/response logging for compliance and debugging

### Developer Experience
- **Interactive API Documentation**: SpringDoc OpenAPI 3 with Swagger UI for all services
- **Hot Reload Development**: Vite-powered frontend with instant updates
- **Containerized Deployment**: Docker Compose for consistent development and production environments
- **Extensive Documentation**: HLD, LLD, code walkthroughs, and interview preparation guides
- **Modern Tooling**: Latest Java 21 features including records and virtual threads

## 🏗️ Architecture

### System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         React Web Portal (Port 5173)              │
│                    Multi-step Return Order Workflow               │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP/HTTPS
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway (Port 8080)                      │
│              Spring Cloud Gateway - Edge Routing                 │
└────────────────────────────┬────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Eureka     │    │    Config    │    │    Kafka     │
│   Server     │    │   Server     │    │   Cluster    │
│   (8761)     │    │   (8888)     │    │   (9092)     │
└──────────────┘    └──────────────┘    └──────────────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
        ┌────────────────────┼────────────────────┬──────────────┐
        ▼                    ▼                    ▼              ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐  ┌──────────┐
│    Auth      │    │  Component   │    │   Payment    │  │ Notification│
│   Service    │    │   Processing │    │   Service    │  │  Service  │
│   (8084)     │    │   (8081)     │    │   (8083)     │  │  (8085)   │
└──────────────┘    └──────────────┘    └──────────────┘  └──────────┘
                             │
                             ▼
                    ┌──────────────┐
                    │ Packaging &  │
                    │  Delivery   │
                    │   Service   │
                    │   (8082)    │
                    └──────────────┘
```

### Microservices Breakdown

| Service | Port | Responsibility | Key Technologies |
|---------|------|----------------|------------------|
| **eurekaserver** | 8761 | Service registry & discovery | Netflix Eureka |
| **configserver** | 8888 | Centralized configuration management | Spring Cloud Config |
| **jwtauthenticationservice** | 8084 | JWT authentication & authorization | Spring Security, JJWT |
| **componentprocessingservice** | 8081 | Core business logic & orchestration | OpenFeign, Strategy Pattern |
| **packaginganddeliveryservice** | 8082 | Packaging & delivery tariff calculation | REST API |
| **paymentservice** | 8083 | Payment processing & Stripe integration | Stripe SDK, Kafka Producer |
| **notificationservice** | 8085 | Event-driven customer notifications | Kafka Consumer |
| **apigateway** | 8080 | Edge gateway & intelligent routing | Spring Cloud Gateway |
| **webportal** | 5173 | React SPA frontend | React 19, Vite |

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.4.2
- **Language**: Java 21 LTS
- **Cloud**: Spring Cloud 2024.0.0
- **Security**: Spring Security 6 + JJWT 0.12.6
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Message Broker**: Apache Kafka 7.5.0
- **Database**: H2 (development), PostgreSQL/MySQL (production recommended)
- **Payment**: Stripe Java SDK 28.0.0
- **Documentation**: SpringDoc OpenAPI 3
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose

### Frontend
- **Framework**: React 19.2.8
- **Build Tool**: Vite 8.2.0
- **HTTP Client**: Axios 1.19.0
- **Icons**: Lucide React 1.31.0
- **State Management**: React Context API
- **Linting**: Oxlint 1.75.0

## 📋 Prerequisites

Before you begin, ensure you have the following installed:
- **Java JDK 21+** ([Download](https://adoptium.net/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Node.js 18+** ([Download](https://nodejs.org/))
- **Docker & Docker Compose** ([Download](https://www.docker.com/get-started))
- **Git** ([Download](https://git-scm.com/downloads))

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/PackageDelivery.git
cd PackageDelivery
```

### 2. Start Infrastructure Services

```bash
# Start Kafka and Zookeeper
docker-compose -f docker-compose.kafka.yml up -d

# Start all microservices
docker-compose up -d
```

### 3. Verify Service Health

```bash
# Check Eureka Dashboard
open http://localhost:8761

# Check API Gateway
curl http://localhost:8080/actuator/health

# Check individual services
curl http://localhost:8084/actuator/health  # Auth Service
curl http://localhost:8081/actuator/health  # Component Processing
```

### 4. Access the Application

- **Web Portal**: http://localhost:5173
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **Swagger Documentation**: http://localhost:8084/swagger-ui.html

### 5. Default Credentials

```
Username: admin
Password: admin
```

## 📖 API Documentation

### Authentication Endpoints

#### Login
```http
POST /login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

#### Validate Token
```http
GET /validate
Authorization: Bearer <JWT_TOKEN>
```

### Component Processing Endpoints

#### Process Return Request
```http
POST /service
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "userName": "john_doe",
  "contactNumber": 9876543210,
  "creditCardNumber": 1234567890123456,
  "componentType": "integral",
  "componentName": "MacBook Pro M3 Display",
  "quantityOfDefective": 1,
  "isPriorityRequest": true
}
```

#### Process Payment
```http
POST /payment/{requestID}/{creditCardNumber}/{creditLimit}/{processingCharge}
Authorization: Bearer <JWT_TOKEN>
```

### Packaging & Delivery Endpoints

#### Calculate Packaging Charges
```http
GET /PackagingAndDeliveryCharge/{componentType}/{count}
```

### Payment Endpoints

#### Process Card Payment
```http
GET /card/{cardNumber}/{charge}
```

#### Execute Stripe Charge
```http
POST /api/v1/payments/stripe-charge?cardNumber={cardNumber}&charge={charge}&currency={currency}
```

### Interactive Documentation
- **Auth Service**: http://localhost:8084/swagger-ui.html
- **Component Processing**: http://localhost:8081/swagger-ui.html
- **Payment Service**: http://localhost:8083/swagger-ui.html
- **Packaging Service**: http://localhost:8082/swagger-ui.html

## 🧪 Testing

### Backend Testing
```bash
# Run tests for individual services
cd jwtAuthentication
mvn test

cd paymentservice
mvn test

cd componentprocessingservice
mvn test
```

### Frontend Testing
```bash
cd webportal
npm run lint
npm run build
```

### Integration Testing
```bash
# Test complete workflow via API Gateway
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

## 🏗️ Development Guide

### Backend Development
```bash
# Run individual service locally
cd jwtAuthentication
mvn spring-boot:run

# Build Docker image
mvn clean package
docker build -t jwtauthenticationservice .
```

### Frontend Development
```bash
cd webportal
npm install
npm run dev
```

### Adding New Microservices
1. Create new Spring Boot project with required dependencies
2. Add Eureka client configuration
3. Configure API Gateway routing
4. Update Docker Compose file
5. Add service documentation

## 🚢 Deployment

### Production Deployment

#### Environment Variables
```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/packagedelivery
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=secure_password

# Security
JWT_SECRET=your-secure-secret-key-min-256-bits
STRIPE_API_KEY=sk_live_your_stripe_key

# Service Discovery
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-broker:9092
```

#### Docker Production Build
```bash
# Build production images
docker-compose -f docker-compose.prod.yml build

# Deploy to production
docker-compose -f docker-compose.prod.yml up -d
```

#### Kubernetes Deployment (Optional)
```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n packagedelivery
```

## 📊 Monitoring & Logging

### Application Monitoring
- **Spring Boot Actuator**: `/actuator` endpoints for health checks
- **Eureka Dashboard**: Service registration status
- **Custom Metrics**: Request processing times, payment success rates

### Logging
- **Format**: JSON-structured logging for production
- **Levels**: Configurable per service (ERROR, WARN, INFO, DEBUG)
- **Aggregation**: Recommended ELK stack or CloudWatch for production

## 🔒 Security Considerations

### Current Implementation
- ✅ JWT-based authentication
- ✅ Token validation across services
- ✅ CORS configuration
- ✅ Spring Security 6 integration

### Production Recommendations
- ⚠️ **Upgrade Password Encoding**: Replace `NoOpPasswordEncoder` with BCrypt
- ⚠️ **Secure JWT Secret**: Use environment variables for secret management
- ⚠️ **Database Security**: Migrate from H2 to PostgreSQL/MySQL
- ⚠️ **CORS Restriction**: Limit allowed origins in production
- ⚠️ **HTTPS**: Enable TLS for all communications
- ⚠️ **API Rate Limiting**: Implement rate limiting in API Gateway
- ⚠️ **Input Validation**: Add comprehensive request validation
- ⚠️ **Dependency Scanning**: Regular security audits

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit your changes** (`git commit -m 'Add amazing feature'`)
4. **Push to the branch** (`git push origin feature/amazing-feature`)
5. **Open a Pull Request**

### Development Standards
- Follow Java Code Conventions
- Write unit tests for new features
- Update API documentation
- Ensure Docker builds successfully
- Add comments for complex logic

## 📚 Documentation

### Architecture Documentation
- [High-Level Design (HLD)](jwtAuthentication/Documentation/01_jwt_auth_hld.md)
- [Low-Level Design (LLD)](jwtAuthentication/Documentation/02_jwt_auth_lld.md)
- [Code Walkthroughs](jwtAuthentication/Documentation/03_jwt_auth_code_walkthrough.md)
- [Testing Guides](jwtAuthentication/Documentation/04_jwt_auth_testing_guide.md)
- [Interview Preparation](jwtAuthentication/Documentation/05_jwt_auth_interview_prep.md)

### Service-Specific Documentation
- [JWT Authentication Service](jwtAuthentication/Documentation/)
- [Component Processing Service](componentprocessingservice/Documentation/)
- [Payment Service](paymentservice/Documentation/)
- [Packaging & Delivery Service](packaginganddeliveryservice/Documentation/)
- [Web Portal](webportal/Documentation/)

## 🗺️ Roadmap

### Phase 1: Production Hardening
- [ ] BCrypt password encoding
- [ ] PostgreSQL migration
- [ ] Enhanced security configuration
- [ ] Circuit breaker implementation

### Phase 2: Enhanced Features
- [ ] Real-time order tracking
- [ ] Advanced analytics dashboard
- [ ] Multi-currency support
- [ ] Mobile application (React Native)

### Phase 3: Enterprise Features
- [ ] Multi-tenant support
- [ ] Advanced reporting
- [ ] Integration with ERP systems
- [ ] AI-powered return prediction

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Lead Developer**: [Your Name]
- **Architecture**: [Your Name]
- **Contributors**: [List of contributors]

## 🙏 Acknowledgments

- Spring Boot team for the amazing framework
- React community for excellent UI library
- Stripe for payment gateway integration
- All contributors and open-source projects used

## 📞 Support

- **Documentation**: [Full Documentation](https://your-docs-site.com)
- **Issues**: [GitHub Issues](https://github.com/yourusername/PackageDelivery/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/PackageDelivery/discussions)
- **Email**: support@packagedelivery.com

## 🔗 Links

- **Live Demo**: [https://demo.packagedelivery.com](https://demo.packagedelivery.com)
- **API Documentation**: [https://api.packagedelivery.com/docs](https://api.packagedelivery.com/docs)
- **Blog**: [https://blog.packagedelivery.com](https://blog.packagedelivery.com)

---

**Built with ❤️ using Spring Boot, Java 21, and React 19**
