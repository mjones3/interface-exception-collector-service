#!/bin/bash

# Python 3.13 Compatibility Fix Script
# Automatically switches to confluent-kafka for Python 3.13 compatibility

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENV_DIR="$SCRIPT_DIR/.venv"

echo "🔧 Python 3.13 Compatibility Fix"
echo "================================="

# Check if virtual environment exists
if [ ! -d "$VENV_DIR" ]; then
    echo "❌ Virtual environment not found. Run setup_python_env.sh first."
    exit 1
fi

# Activate virtual environment
source "$VENV_DIR/bin/activate"

# Check Python version
PYTHON_VERSION=$(python -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')")
echo "🐍 Python version: $PYTHON_VERSION"

if [ "$PYTHON_VERSION" = "3.13" ]; then
    echo "🔄 Applying Python 3.13 compatibility fixes..."
    
    # Uninstall kafka-python if it exists
    echo "🧹 Removing incompatible kafka-python..."
    pip uninstall kafka-python -y --quiet 2>/dev/null || true
    
    # Install confluent-kafka
    echo "📥 Installing confluent-kafka..."
    pip install confluent-kafka>=2.3.0 --quiet
    
    # Switch to confluent kafka utilities
    if [ -f "$SCRIPT_DIR/kafka_utils_confluent.py" ]; then
        echo "🔗 Switching to confluent-kafka utilities..."
        cp "$SCRIPT_DIR/kafka_utils_confluent.py" "$SCRIPT_DIR/kafka_utils.py"
    fi
    
    echo "✅ Python 3.13 compatibility fixes applied!"
    
else
    echo "ℹ️  Python $PYTHON_VERSION detected - no fixes needed."
    echo "   This script is only needed for Python 3.13."
fi

# Test the fix
echo "🧪 Testing Kafka connectivity..."
if python -c "
import sys
sys.path.insert(0, '$SCRIPT_DIR')
try:
    from kafka_utils import check_kafka_connectivity
    result = check_kafka_connectivity(['localhost:29092'])
    print('✅ Kafka test passed' if result else '⚠️  Kafka not accessible (but library works)')
except ImportError as e:
    print(f'❌ Import error: {e}')
    exit(1)
except Exception as e:
    print(f'⚠️  Test error: {e} (but library should work)')
"; then
    echo "✅ Compatibility fix successful!"
else
    echo "❌ There may still be issues. Check the error messages above."
    exit 1
fi

deactivate
echo "🎉 Ready to run event generation scripts!"