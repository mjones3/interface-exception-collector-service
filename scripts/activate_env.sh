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
