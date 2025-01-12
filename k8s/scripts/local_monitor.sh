# View logs
kubectl logs -f deployment/reactive-spring

# View deployment status
kubectl describe deployment reactive-spring

# View pod status
kubectl get pods -w