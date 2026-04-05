@echo off
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME:~0,-1%
"%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*
