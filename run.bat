@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo [1/2] 正在构建...
call mvn -B clean package
if errorlevel 1 (
    echo 构建失败，请检查是否安装了 JDK 17+ 和 Maven。
    pause
    exit /b 1
)
echo [2/2] 启动游戏...
java -jar target\plane-war-2.0.0.jar
if errorlevel 1 pause
