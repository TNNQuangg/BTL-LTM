@echo off
cd /d "%~dp0server"
if not exist data mkdir data
if not defined SERVER_ADDRESS set "SERVER_ADDRESS=0.0.0.0"
echo Server dang chay. Giu cua so nay mo trong suot buoi demo.
java -jar server.jar
if errorlevel 1 pause
