#!/bin/bash

echo "🔧 Running Java JWT Test Generator..."

# Copy the Java file to the test directory and run it with Maven
cp JwtTestGenerator.java interface-exception-collector/src/test/java/
cd interface-exception-collector

# Compile and run using Maven
mvn test-compile exec:java -Dexec.mainClass="JwtTestGenerator" -Dexec.classpathScope="test" -q

# Clean up
rm src/test/java/JwtTestGenerator.java