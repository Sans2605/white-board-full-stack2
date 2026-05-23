@REM Maven Wrapper for Windows
@echo off
setlocal

WHERE mvn >nul 2>nul
IF %ERRORLEVEL% == 0 (
  mvn %*
  EXIT /B %ERRORLEVEL%
)

echo ERROR: Maven not found. Please install Maven: https://maven.apache.org/install.html
EXIT /B 1
