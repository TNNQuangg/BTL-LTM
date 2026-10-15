@echo off
cd /d "%~dp0server"
if not exist data mkdir data
echo Server dang chay. Giu cua so nay mo trong suot buoi demo.
java -jar server.jar
if errorlevel 1 pause
