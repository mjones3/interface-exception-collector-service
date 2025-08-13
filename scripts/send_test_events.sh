#!/bin/bash

# Blood Banking Order Rejection Event Generator
# Sends 10 realistic OrderRejected events to Kafka for testing

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENV_DIR="$SCRIPT_DIR/.venv"

echo "🩸 BioPro Order Rejection Event Generator"
echo "================================================"

# Check if Python 3 is available
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is required but not installed."
    echo "   Please install Python 3 and try again."
    exit 1
fi

# Function to setup virtual environment
setup_venv() {
    echo "📦 Setting up Python virtual environment..."
    
    # Remove existing venv if it's corrupted
    if [ -d "$VENV_DIR" ] && [ ! -f "$VENV_DIR/pyvenv.cfg" ]; then
        echo "🧹 Removing corrupted virtual environment..."
        rm -rf "$VENV_DIR"
    fi
    
    # Create virtual environment if it doesn't exist
    if [ ! -d "$VENV_DIR" ]; then
        echo "🔨 Creating new virtual environment..."
        python3 -m venv "$VENV_DIR"
        
        # Verify creation was successful
        if [ ! -f "$VENV_DIR/bin/activate" ]; then
            echo "❌ Failed to create virtual environment"
            exit 1
        fi
    fi
    
    # Activate virtual environment
    echo "🔧 Activating virtual environment..."
    source "$VENV_DIR/bin/activate"
    
    # Upgrade pip to latest version
    echo "⬆️  Upgrading pip..."
    python -m pip install --upgrade pip --quiet
    
    # Install dependencies with Python version compatibility
    echo "📥 Installing dependencies..."
    PYTHON_VERSION=$(python -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')")
    
    if [ "$PYTHON_VERSION" = "3.13" ]; then
        echo "🐍 Python 3.13 detected, using compatible dependencies..."
        if [ -f "$SCRIPT_DIR/requirements-py313.txt" ]; then
            pip install -r "$SCRIPT_DIR/requirements-py313.txt" --quiet
            # Use confluent kafka utilities for Python 3.13
            if [ -f "$SCRIPT_DIR/kafka_utils_confluent.py" ]; then
                cp "$SCRIPT_DIR/kafka_utils_confluent.py" "$SCRIPT_DIR/kafka_utils.py"
            fi
        else
            echo "⚠️  Python 3.13 requirements file not found, trying standard requirements..."
            pip install -r "$SCRIPT_DIR/requirements.txt" --quiet || {
                echo "❌ Failed to install dependencies for Python 3.13"
                echo "💡 Try running: ./fix_py313_compatibility.sh"
                exit 1
            }
        fi
    else
        pip install -r "$SCRIPT_DIR/requirements.txt" --quiet
    fi
    
    echo "✅ Virtual environment ready!"
}

# Setup virtual environment
setup_venv

# Check if Kafka is running
echo "🔍 Checking Kafka connectivity..."
if ! python -c "
import sys
sys.path.insert(0, '$SCRIPT_DIR')
from kafka_utils import check_kafka_connectivity
try:
    if check_kafka_connectivity(['localhost:9092']):
        print('✅ Kafka is accessible')
    else:
        print('❌ Cannot connect to Kafka')
        print('   Make sure Kafka is running on localhost:9092')
        print('   You can start it with: docker-compose up -d kafka')
        sys.exit(1)
except Exception as e:
    print(f'❌ Error checking Kafka: {e}')
    print('   This might be a dependency issue.')
    print('   For Python 3.13, try: ./fix_py313_compatibility.sh')
    sys.exit(1)
"; then
    exit 1
fi

# Run the event generator
echo "🚀 Running event generator..."
cd "$SCRIPT_DIR" && python send_order_rejected_events.py

# Check if script ran successfully
if [ $? -eq 0 ]; then
    echo ""
    echo "🎯 Next Steps:"
    echo "   1. Check your application logs to see events being processed"
    echo "   2. Query the API to see captured exceptions:"
    echo "      curl -H 'Authorization: Bearer <your-jwt-token>' http://localhost:8080/api/v1/exceptions"
    echo "   3. Use the Bruno collection to test the API endpoints"
else
    echo "❌ Event generator failed. Check the error messages above."
    deactivate
    exit 1
fi

# Deactivate virtual environment
echo "🔄 Deactivating virtual environment..."
deactivate

echo "✨ Script completed successfully!"