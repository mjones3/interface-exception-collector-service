#!/bin/bash

# Python Environment Setup Script
# Creates and configures virtual environment for all Python scripts

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENV_DIR="$SCRIPT_DIR/.venv"
REQUIREMENTS_FILE="$SCRIPT_DIR/requirements.txt"

echo "🐍 Python Environment Setup"
echo "============================"

# Function to check Python version
check_python_version() {
    if ! command -v python3 &> /dev/null; then
        echo "❌ Python 3 is required but not installed."
        echo "   Please install Python 3.8 or higher and try again."
        echo ""
        echo "   Installation options:"
        echo "   - macOS: brew install python3"
        echo "   - Ubuntu/Debian: sudo apt-get install python3 python3-venv python3-pip"
        echo "   - CentOS/RHEL: sudo yum install python3 python3-venv python3-pip"
        exit 1
    fi
    
    # Check Python version (require 3.8+)
    PYTHON_VERSION=$(python3 -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')")
    REQUIRED_VERSION="3.8"
    
    if [ "$(printf '%s\n' "$REQUIRED_VERSION" "$PYTHON_VERSION" | sort -V | head -n1)" != "$REQUIRED_VERSION" ]; then
        echo "❌ Python $PYTHON_VERSION detected, but Python $REQUIRED_VERSION or higher is required."
        exit 1
    fi
    
    echo "✅ Python $PYTHON_VERSION detected"
}

# Function to create virtual environment
create_venv() {
    echo "📦 Creating virtual environment..."
    
    # Remove existing venv if requested or corrupted
    if [ -d "$VENV_DIR" ]; then
        if [ "$1" = "--force" ] || [ ! -f "$VENV_DIR/pyvenv.cfg" ]; then
            echo "🧹 Removing existing virtual environment..."
            rm -rf "$VENV_DIR"
        else
            echo "ℹ️  Virtual environment already exists. Use --force to recreate."
            return 0
        fi
    fi
    
    # Create new virtual environment
    echo "🔨 Creating new virtual environment at $VENV_DIR..."
    python3 -m venv "$VENV_DIR"
    
    # Verify creation was successful
    if [ ! -f "$VENV_DIR/bin/activate" ]; then
        echo "❌ Failed to create virtual environment"
        exit 1
    fi
    
    echo "✅ Virtual environment created successfully"
}

# Function to activate and setup environment
setup_environment() {
    echo "🔧 Setting up Python environment..."
    
    # Activate virtual environment
    source "$VENV_DIR/bin/activate"
    
    # Upgrade pip to latest version
    echo "⬆️  Upgrading pip..."
    python -m pip install --upgrade pip --quiet
    
    # Install wheel for better package installation
    echo "🛞 Installing wheel..."
    pip install wheel --quiet
    
    # Install dependencies with Python version compatibility
    if [ -f "$REQUIREMENTS_FILE" ]; then
        echo "📥 Installing dependencies from requirements.txt..."
        
        # Check Python version and use appropriate requirements
        PYTHON_VERSION=$(python -c "import sys; print(f'{sys.version_info.major}.{sys.version_info.minor}')")
        
        if [ "$PYTHON_VERSION" = "3.13" ] && [ -f "$SCRIPT_DIR/requirements-py313.txt" ]; then
            echo "🐍 Python 3.13 detected, using compatible requirements..."
            pip install -r "$SCRIPT_DIR/requirements-py313.txt" --quiet
            
            # Create a symlink to use confluent kafka utilities
            if [ -f "$SCRIPT_DIR/kafka_utils_confluent.py" ]; then
                echo "🔗 Using Confluent Kafka utilities for Python 3.13 compatibility..."
                ln -sf kafka_utils_confluent.py "$SCRIPT_DIR/kafka_utils.py" 2>/dev/null || cp "$SCRIPT_DIR/kafka_utils_confluent.py" "$SCRIPT_DIR/kafka_utils.py"
            fi
        else
            # Try standard requirements first
            if ! pip install -r "$REQUIREMENTS_FILE" --quiet; then
                echo "⚠️  Standard requirements failed, trying Python 3.13 compatible version..."
                if [ -f "$SCRIPT_DIR/requirements-py313.txt" ]; then
                    pip install -r "$SCRIPT_DIR/requirements-py313.txt" --quiet
                    ln -sf kafka_utils_confluent.py "$SCRIPT_DIR/kafka_utils.py" 2>/dev/null || cp "$SCRIPT_DIR/kafka_utils_confluent.py" "$SCRIPT_DIR/kafka_utils.py"
                else
                    echo "❌ Failed to install dependencies"
                    exit 1
                fi
            fi
        fi
        
        # Show installed packages
        echo "📋 Installed packages:"
        pip list --format=columns
    else
        echo "⚠️  No requirements.txt found. Skipping dependency installation."
    fi
    
    # Deactivate for now
    deactivate
    
    echo "✅ Environment setup complete!"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --force    Force recreation of virtual environment"
    echo "  --help     Show this help message"
    echo ""
    echo "This script creates and configures a Python virtual environment"
    echo "for all Python scripts in this directory."
}

# Function to create activation helper
create_activation_helper() {
    cat > "$SCRIPT_DIR/activate_env.sh" << 'EOF'
#!/bin/bash
# Helper script to activate the Python virtual environment

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENV_DIR="$SCRIPT_DIR/.venv"

if [ ! -d "$VENV_DIR" ]; then
    echo "❌ Virtual environment not found. Run setup_python_env.sh first."
    exit 1
fi

echo "🔧 Activating Python virtual environment..."
source "$VENV_DIR/bin/activate"

echo "✅ Virtual environment activated!"
echo "💡 To deactivate, run: deactivate"
echo "🐍 Python: $(which python)"
echo "📦 Pip: $(which pip)"

# Keep the shell open with the activated environment
exec "$SHELL"
EOF
    
    chmod +x "$SCRIPT_DIR/activate_env.sh"
    echo "📝 Created activation helper: activate_env.sh"
}

# Main execution
main() {
    local force_recreate=false
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --force)
                force_recreate=true
                shift
                ;;
            --help)
                show_usage
                exit 0
                ;;
            *)
                echo "❌ Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Execute setup steps
    check_python_version
    
    if [ "$force_recreate" = true ]; then
        create_venv --force
    else
        create_venv
    fi
    
    setup_environment
    create_activation_helper
    
    echo ""
    echo "🎉 Python environment setup complete!"
    echo ""
    echo "📁 Virtual environment location: $VENV_DIR"
    echo "🔧 To activate manually: source $VENV_DIR/bin/activate"
    echo "🚀 To run scripts: ./send_test_events.sh"
    echo "💡 Helper script: ./activate_env.sh"
    echo ""
    echo "✨ You're ready to run Python scripts!"
}

# Run main function
main "$@"