@echo off
title Running Vasudha Tiffin Billing System
echo Executing application launcher...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run.ps1"
echo.
echo Application stopped.
pause
