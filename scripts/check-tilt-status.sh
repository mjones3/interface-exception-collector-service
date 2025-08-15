#!/bin/bash

# Quick script to check Tilt and Kubernetes status for BioPro services

echo "🚀 BioPro Services Status Check"
echo "==============================="

# Check if Tilt is running
if command -v tilt &> /dev/null; then
    echo "✅ Tilt is installed"
    
    # Check Tilt status
    echo ""
    echo "📊 Tilt Status:"
    echo "==============="
    tilt status 2>/dev/null || echo "❌ Tilt is not running. Run 'tilt up' to start."
else
    echo "❌ Tilt is not installed"
fi

echo ""
echo "🏗️  Kubernetes Resources:"
echo "========================="

# Check if kubectl is available
if command -v kubectl &> /dev/null; then
    echo "✅ kubectl is available"
    
    # Check current context
    CURRENT_CONTEXT=$(kubectl config current-context 2>/dev/null || echo "none")
    echo "📍 Current context: $CURRENT_CONTEXT"
    
    echo ""
    echo "📦 Pods Status:"
    echo "==============="
    kubectl get pods -o wide 2>/dev/null || echo "❌ Cannot connect to Kubernetes cluster"
    
    echo ""
    echo "🔗 Services:"
    echo "============"
    kubectl get services 2>/dev/null || echo "❌ Cannot get services"
    
    echo ""
    echo "🏥 Service Health (if running):"
    echo "==============================="
    
    # Check Partner Order Service
    POS_POD=$(kubectl get pods -l app=partner-order-service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    if [[ -n "$POS_POD" ]]; then
        echo "🏪 Partner Order Service ($POS_POD):"
        kubectl exec "$POS_POD" -- curl -s http://localhost:8090/actuator/health 2>/dev/null | jq '.status' 2>/dev/null || echo "   ❌ Health check failed"
    else
        echo "❌ Partner Order Service pod not found"
    fi
    
    # Check Interface Exception Collector
    IEC_POD=$(kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    if [[ -n "$IEC_POD" ]]; then
        echo "🔍 Interface Exception Collector ($IEC_POD):"
        kubectl exec "$IEC_POD" -- curl -s http://localhost:8080/actuator/health 2>/dev/null | jq '.status' 2>/dev/null || echo "   ❌ Health check failed"
    else
        echo "❌ Interface Exception Collector pod not found"
    fi
    
    # Check Kafka
    KAFKA_POD=$(kubectl get pods -l app=kafka -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
    if [[ -n "$KAFKA_POD" ]]; then
        echo "📨 Kafka ($KAFKA_POD): ✅ Running"
    else
        echo "❌ Kafka pod not found"
    fi
    
else
    echo "❌ kubectl is not available"
fi

echo ""
echo "🌐 Port Forwards (if Tilt is running):"
echo "======================================"
echo "• Partner Order Service: http://localhost:8090"
echo "• Interface Exception Collector: http://localhost:8080"
echo "• Kafka UI: http://localhost:8081"

echo ""
echo "🎯 Next Steps:"
echo "=============="
echo "1. If services are not running: tilt up"
echo "2. To debug Kafka events: ./scripts/debug-kafka-events.sh"
echo "3. To view logs: kubectl logs -f deployment/partner-order-service"
echo "4. To test order submission: curl -X POST -H 'Content-Type: application/json' -d '{...}' http://localhost:8090/v1/partner-order-provider/orders"