@echo off
setlocal

REM 获取脚本所在目录
set "SCRIPT_DIR=%~dp0"

REM 使用项目内的settings.xml运行Maven
call "%SCRIPT_DIR%mvnw" -s "%SCRIPT_DIR%.mvn/settings.xml" %*