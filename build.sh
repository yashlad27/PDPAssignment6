#!/bin/bash

# Build script for Calendar Application
echo "Cleaning and building project..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
  echo "Build successful! JAR created at: target/PDPAssignment6-1.0-SNAPSHOT.jar"
  echo "To run the application, use: java -jar target/PDPAssignment6-1.0-SNAPSHOT.jar"
else
  echo "Build failed. Please check the errors above."
  exit 1
fi 