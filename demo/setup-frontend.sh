#!/bin/bash
echo "Setting up the frontend for the Admin Dashboard..."

cd src/main/frontend
echo "Installing frontend dependencies..."
npm install --legacy-peer-deps

echo "Building the frontend..."
npm run build

echo "Creating static directory if it doesn't exist..."
mkdir -p ../resources/static

echo "Copying frontend build to static resources..."
cp -r build/* ../resources/static/

echo "Frontend setup complete!"
echo "You can now run the application using:"
echo "mvn spring-boot:run"
echo "or"
echo "java -jar target/demo-0.0.1-SNAPSHOT.jar"

cd ../../.. 