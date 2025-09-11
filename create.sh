#!/bin/bash

# Create root files
touch docker-compose.yml docker-compose.override.yml .env.example .gitignore README.md

# Create shared structure
mkdir -p shared/common-libs/{domain-common,messaging-common,web-common}
mkdir -p shared/proto-definitions

# Create infrastructure structure
mkdir -p infrastructure/{docker,k8s,monitoring}

# List of services
services=(order inventory payment notification analytics user)

# Create service structure
for service in "${services[@]}"; do
  base="services/${service}-service"
  mkdir -p $base/src/{main/java/com/ecommerce/$service,main/resources,test}
  touch $base/{Dockerfile,pom.xml}
done

echo "✅ Project structure created successfully!"
