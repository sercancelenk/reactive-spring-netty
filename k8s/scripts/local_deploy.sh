#!/bin/bash

# Connect to OrbStack's Docker context
echo "Connecting to OrbStack..."
docker context use orb

# Stop and remove any existing containers using the image
echo "Cleaning up existing containers..."
docker ps -a | grep 'reactive-spring' | awk '{print $1}' | xargs -r docker rm -f

# Remove existing images
echo "Removing old images..."
docker images | grep 'reactive-spring' | awk '{print $3}' | xargs -r docker rmi -f

# Start local registry if not running
echo "Ensuring local registry is running..."
if ! docker ps | grep -q 'registry:2'; then
    docker run -d -p 5000:5000 --name registry registry:2
fi

# Build and tag the image for local registry
echo "Building and pushing Docker image..."
docker build -t localhost:5000/reactive-spring:latest .
docker push localhost:5000/reactive-spring:latest

# Delete existing deployment and service if they exist
echo "Cleaning up existing Kubernetes resources..."
kubectl delete deployment reactive-spring --ignore-not-found
kubectl delete service reactive-spring --ignore-not-found

# Apply Kubernetes configurations
echo "Deploying to Kubernetes..."
kubectl apply -f ../deployment.yaml
kubectl apply -f ../service.yaml

# Wait for deployment to be ready
echo "Waiting for deployment to be ready..."
kubectl wait --for=condition=available deployment/reactive-spring --timeout=120s

# Show status
echo "Deployment status:"
kubectl get pods
kubectl get services

echo "Application is accessible at http://localhost:30080"

# Optional: Show logs
echo "Showing logs (press Ctrl+C to exit)..."
kubectl logs -f deployment/reactive-spring 