#!/bin/bash

# Script to prepare Maven build for Tilt development

set -e

echo "🔧 Preparing Maven build for Tilt..."

# Create target directories if they don't exist
mkdir -p target/classes
mkdir -p target/lib

# Copy dependencies to target/lib if not already there
if [ ! -d "target/lib" ] || [ -z "$(ls -A target/lib)" ]; then
    echo "📦 Copying Maven dependencies..."
    mvn dependency:copy-dependencies -DoutputDirectory=target/lib -DincludeScope=runtime -q
fi

# Compile the application
echo "🏗️  Compiling application..."
mvn compile -q

echo "✅ Build preparation complete!"
echo "📁 Classes: target/classes"
echo "📚 Dependencies: target/lib"