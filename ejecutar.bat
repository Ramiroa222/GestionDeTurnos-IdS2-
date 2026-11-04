@echo off
cd /d "%~dp0"

echo Compilando el proyecto...
call mvn clean package
if errorlevel 1 (
    echo.
    echo Error al compilar. Verifica que Java y Maven esten instalados.
    pause
    exit /b 1
)

echo.
echo Iniciando la aplicacion...
call mvn exec:java

echo.
pause