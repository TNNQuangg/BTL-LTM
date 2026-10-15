@echo off
cd /d "%~dp0client"
set "SERVER_HOST=%~1"
if not defined SERVER_HOST set /p SERVER_HOST=Nhap IP may chu (vi du 192.168.1.180:8080):
if not defined SERVER_HOST set "SERVER_HOST=localhost:8080"
java "-Dscribble.server=%SERVER_HOST%" -cp "client.jar;lib/*" com.nhom8.client.Launcher
if errorlevel 1 pause
