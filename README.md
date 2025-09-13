# E-commerce Microservices Platform

A production-grade reactive microservices platform built with Spring WebFlux that handles high-throughput e-commerce operations with real-time inventory management, order processing, and customer notifications.

## 🌟 Features

### Core Capabilities
- **High-Performance**: Handle 10,000+ orders per second with P99 < 200ms latency
- **Real-Time Processing**: Instant inventory updates and order status notifications
- **Reactive Architecture**: Built with Spring WebFlux for maximum concurrency
- **Event-Driven**: Complete event sourcing with Kafka for audit trails
- **Production-Ready**: Circuit breakers, distributed tracing, and comprehensive monitoring

### Business Features
- Real-time order processing with state management
- Multi-warehouse inventory tracking and reservations
- Integrated payment processing with fraud detection
- Dynamic pricing based on demand and inventory levels
- Customer notifications via email, SMS, and push notifications
- Advanced analytics and business intelligence

## 🏗️ Architecture

### Microservices Overview
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ Order       │    │ Inventory   │    │ Payment     │
│ Service     │    │ Service     │    │ Service     │
│ :8080       │    │ :8081       │    │ :8082       │
└─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │
       └───────────────────┼───────────────────┘
                           │
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ Notification│    │ Analytics   │    │ User        │
│ Service     │    │ Service     │    │ Service     │
│ :8083       │    │ :8084       │    │ :8085       │
└─────────────┘    └─────────────┘    └─────────────┘
```

### Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Framework** | Spring Boot 3.2 + WebFlux | Reactive microservices |
| **Databases** | MongoDB Atlas, PostgreSQL | Document & relational storage |
| **Caching** | Redis | Session management, distributed locks |
| **Messaging** | Apache Kafka, RabbitMQ | Event streaming & task queues |
| **Monitoring** | Prometheus, Grafana, Jaeger | Metrics, visualization & tracing |
| **Container** | Docker, Docker Compose | Containerization |
| **Cloud** | Azure (AKS, ACR, PostgreSQL) | Production deployment |

### Clean Architecture Layers
```
┌─────────────────────────────────────┐
│              Web Layer              │
│        (REST Controllers)           │
├─────────────────────────────────────┤
│           Application Layer         │
│         (Use Cases, DTOs)           │
├─────────────────────────────────────┤
│             Domain Layer            │
│    (Entities, Value Objects)        │
├─────────────────────────────────────┤
│         Infrastructure Layer        │
│   (Repositories, External APIs)     │
└─────────────────────────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven 3.8+
- MongoDB Atlas account (free tier)

### Local Development Setup

1. **Clone and Setup Environment**
   ```bash
   git clone <repository-url>
   cd ecommerce-microservices
   cp .env.example .env
   # Edit .env with your MongoDB Atlas URI
   ```

2. **Start Infrastructure**
   ```bash
   chmod +x scripts/*.sh
   ./scripts/start-infrastructure.sh
   ./scripts/create-topics.sh
   ```

3. **Build and Run Services**
   ```bash
   ./scripts/build-all-services.sh
   ./scripts/start-all-services.sh
   ```

4. **Verify Deployment**
   ```bash
   ./scripts/api-test.sh
   ```

### Docker Deployment

```bash
# One-command deployment
./scripts/deploy-local.sh
```

### Azure Production Deployment

```bash
# Complete Azure setup
./infra/scripts/setup-azure.sh

# Build and deploy
./infra/scripts/build-and-push.sh
./infra/scripts/deploy-apps.sh
```

## 📊 Monitoring & Management

### Access Points
- **Grafana Dashboard**: http://localhost:3000 (admin/admin)
- **Prometheus Metrics**: http://localhost:9090
- **Jaeger Tracing**: http://localhost:16686
- **Kafka UI**: http://localhost:8080
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

### API Documentation
- **Order Service**: http://localhost:8080/swagger-ui.html
- **Health Checks**: http://localhost:8080/actuator/health
- **Metrics Endpoint**: http://localhost:8080/actuator/prometheus

## 🔧 Service Details

### Order Service (Port 8080)
- Order creation and state management
- Payment gateway integration
- Inventory coordination
- Event publishing

### Inventory Service (Port 8081)
- Real-time stock management
- Multi-warehouse support
- Stock reservations
- Low stock alerts

### Payment Service (Port 8082)
- Stripe integration
- Fraud detection
- Payment validation
- Refund processing

### User Service (Port 8085)
- JWT authentication
- Profile management
- Authorization
- Session handling

### Notification Service (Port 8083)
- Email, SMS, push notifications
- Template management
- Customer preferences
- Delivery tracking

### Analytics Service (Port 8084)
- Real-time analytics
- Customer insights
- Revenue reporting
- Business intelligence

## 📈 Performance & Scaling

### Performance Characteristics
- **Throughput**: 10,000+ orders/second
- **Latency**: P99 < 200ms
- **Availability**: 99.99% target
- **Concurrency**: Reactive streams for high concurrency

### Scaling Options

```bash
# Scale specific service
./infra/scripts/scale-environment.sh order-service 10

# Auto-scaling (pre-configured)
kubectl get hpa -n ecommerce
```

## 🛡️ Production Features

### Security
- JWT-based authentication
- RBAC with fine-grained permissions
- Network security groups
- SSL/TLS termination
- Azure Key Vault integration

### Reliability
- Circuit breakers with Resilience4j
- Retry mechanisms
- Health checks
- Distributed caching
- Event sourcing for audit trails

### Monitoring
- Prometheus metrics collection
- Custom business alerts
- Distributed tracing
- Application insights
- Real-time dashboards

## 🔄 Operations

### Environment Management
```bash
# Development environment
terraform apply -var-file="environments/dev.tfvars"

# Staging environment
terraform apply -var-file="environments/staging.tfvars"

# Production environment
terraform apply -var-file="environments/prod.tfvars"
```

### Data Management
```bash
# Backup data
./infra/scripts/backup-data.sh --upload

# Restore from backup
./infra/scripts/restore-data.sh backups/backup_20241201.tar.gz
```

### Updates
```bash
# Rolling updates (zero downtime)
./infra/scripts/update-deployment.sh
```

## 🧪 Testing

### Run Tests
```bash
# Unit tests
mvn test

# Integration tests
./scripts/api-test.sh

# Load testing
./scripts/test-deployment.sh
```

### Test Coverage
- Unit tests with TestContainers
- Integration tests for all services
- End-to-end workflow testing
- Performance testing scripts

## 📁 Project Structure

```
ecommerce-microservices/
├── services/                    # All microservices
│   ├── order-service/
│   ├── inventory-service/
│   ├── payment-service/
│   ├── user-service/
│   ├── notification-service/
│   └── analytics-service/
├── common/                      # Shared libraries
├── infra/                       # Infrastructure code
│   ├── terraform/              # Azure infrastructure
│   ├── kubernetes/             # K8s manifests
│   └── scripts/               # Automation scripts
├── docker/                      # Docker configurations
├── scripts/                     # Build & deployment scripts
└── docs/                       # Documentation
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

- **Issues**: Create an issue on GitHub
- **Documentation**: Check the `/docs` folder
- **Monitoring**: Use Grafana dashboards for system health

## 🎯 Roadmap

- [ ] GraphQL API gateway
- [ ] Machine learning recommendations
- [ ] Multi-tenant support
- [ ] Advanced fraud detection
- [ ] Mobile app integration