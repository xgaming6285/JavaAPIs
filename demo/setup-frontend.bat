@echo off
echo Setting up the frontend for the Admin Dashboard...

cd src\main\frontend
echo Installing frontend dependencies...
call npm install --legacy-peer-deps

echo Building the frontend...
call npm run build

echo Creating static directory if it doesn't exist...
if not exist ..\resources\static mkdir ..\resources\static

echo Copying frontend build to static resources...
xcopy /E /Y build\* ..\resources\static\

echo Frontend setup complete!
echo You can now run the application using:
echo mvn spring-boot:run
echo or
echo java -jar target/demo-0.0.1-SNAPSHOT.jar

cd ..\..\.. 