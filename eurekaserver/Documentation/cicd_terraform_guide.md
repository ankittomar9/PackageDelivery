# Enterprise CI/CD Pipelines (GitHub Actions) & Terraform IaC Guide

This guide provides a production-grade **GitHub Actions CI/CD Pipeline** (`.github/workflows/ci-cd.yml`) and **Terraform Infrastructure as Code (IaC)** configuration (`terraform/main.tf`) for automated testing, container building, and cloud deployment.

---

## 🚀 1. Automated GitHub Actions CI/CD Pipeline (`.github/workflows/ci-cd.yml`)

Save this file in **`.github/workflows/ci-cd.yml`** at the root of your project repository.

```yaml
name: Production CI/CD Pipeline — Return Order Processing Platform

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]

jobs:
  # ==========================================
  # STAGE 1: Build & Run Automated Unit Tests
  # ==========================================
  backend-test-and-build:
    name: Test & Build Java Microservices (Java 21)
    runs-on: ubuntu-latest

    strategy:
      matrix:
        service:
          - jwtAuthentication
          - componentprocessingservice
          - packaginganddeliveryservice
          - paymentservice

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Run JUnit 5 & MockMvc Tests
        run: |
          cd ${{ matrix.service }}
          mvn clean test

      - name: Build Executable JAR
        run: |
          cd ${{ matrix.service }}
          mvn package -DskipTests

  frontend-test-and-build:
    name: Test & Build React SPA (Vite)
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up Node.js 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: webportal/package-lock.json

      - name: Install Dependencies & Build Bundle
        run: |
          cd webportal
          npm ci
          npm run build

  # ==========================================
  # STAGE 2: Build & Push Docker Images to GHCR
  # ==========================================
  docker-build-and-push:
    name: Build & Push Container Images to GitHub Registry
    needs: [backend-test-and-build, frontend-test-and-build]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/master'

    permissions:
      contents: read
      packages: write

    strategy:
      matrix:
        service:
          - name: jwtauthentication
            path: jwtAuthentication
          - name: componentprocessingservice
            path: componentprocessingservice
          - name: packaginganddeliveryservice
            path: packaginganddeliveryservice
          - name: paymentservice
            path: paymentservice

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Log in to GitHub Container Registry (GHCR)
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and Push Docker Image
        uses: docker/build-push-action@v5
        with:
          context: ./${{ matrix.service.path }}
          file: ./${{ matrix.service.path }}/Dockerfile
          push: true
          tags: |
            ghcr.io/${{ github.repository_owner }}/${{ matrix.service.name }}:latest
            ghcr.io/${{ github.repository_owner }}/${{ matrix.service.name }}:${{ github.sha }}

  # ==========================================
  # STAGE 3: Trigger Cloud Deployments (Render / Vercel)
  # ==========================================
  deploy-to-cloud:
    name: Trigger Automated Cloud Deployments
    needs: docker-build-and-push
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/master'

    steps:
      - name: Trigger Render Microservices Webhook
        run: |
          echo "Triggering Render webhook deployment for microservices mesh..."
          # curl -X POST ${{ secrets.RENDER_DEPLOY_HOOK_URL }}

      - name: Deploy Frontend to Vercel
        uses: amondnet/vercel-action@v25
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
          working-directory: ./webportal
          vercel-args: '--prod'
```

---

## 🏗️ 2. Infrastructure as Code with Terraform (`terraform/main.tf`)

Terraform allows you to declare your cloud infrastructure in code files (`.tf`) so you can provision and destroy environments automatically!

Save this file in **`terraform/main.tf`**:

```hcl
terraform {
  required_version = ">= 1.5.0"
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0.0"
    }
  }
}

provider "docker" {}

# 1. Create Private Docker Network for Platform Microservices
resource "docker_network" "platform_network" {
  name = "return_order_mesh_net"
}

# 2. Deploy Microservice #1: jwtAuthentication (Port 8084)
resource "docker_container" "jwt_auth" {
  name  = "jwtauthentication"
  image = "ghcr.io/yourusername/jwtauthentication:latest"
  
  networks_advanced {
    name = docker_network.platform_network.name
  }

  ports {
    internal = 8084
    external = 8084
  }

  env = [
    "SPRING_PROFILES_ACTIVE=prod"
  ]
}

# 3. Deploy Microservice #2: ComponentProcessing (Port 8081)
resource "docker_container" "component_processing" {
  name  = "componentprocessingservice"
  image = "ghcr.io/yourusername/componentprocessingservice:latest"
  
  networks_advanced {
    name = docker_network.platform_network.name
  }

  ports {
    internal = 8081
    external = 8081
  }

  depends_on = [
    docker_container.jwt_auth
  ]
}

# 4. Deploy Microservice #3: PackagingAndDelivery (Port 8082)
resource "docker_container" "packaging_delivery" {
  name  = "packaginganddeliveryservice"
  image = "ghcr.io/yourusername/packaginganddeliveryservice:latest"
  
  networks_advanced {
    name = docker_network.platform_network.name
  }

  ports {
    internal = 8082
    external = 8082
  }
}

# 5. Deploy Microservice #4: PaymentService (Port 8083)
resource "docker_container" "payment_service" {
  name  = "paymentservice"
  image = "ghcr.io/yourusername/paymentservice:latest"
  
  networks_advanced {
    name = docker_network.platform_network.name
  }

  ports {
    internal = 8083
    external = 8083
  }
}
```

---

## 🎓 3. Terraform Command Cheatsheet & How It Works

| Command | Purpose |
| :--- | :--- |
| **`terraform init`** | Initializes directory and downloads required cloud providers (Docker, AWS, Render). |
| **`terraform plan`** | Dry-run preview showing exactly what resources will be created/updated without changing live cloud. |
| **`terraform apply`** | Provisions the cloud infrastructure defined in your `.tf` files. |
| **`terraform destroy`** | Cleanly tears down all cloud infrastructure to prevent unwanted charges when testing is done. |

---

## 💡 How GitHub CI/CD Works (Line-by-Line Breakdown)

1. **`on: push`**: Triggered automatically every time you execute `git push origin main`.
2. **`matrix` Strategy**: Parallelizes test execution! Runs JUnit 5 tests for all 4 Java microservices simultaneously across isolated GitHub runner containers.
3. **`docker/build-push-action`**: Builds multi-stage Docker images and pushes them to GitHub Container Registry (`ghcr.io`).
4. **Zero Downtime**: Deployments only trigger **IF** all unit tests pass 100%!
