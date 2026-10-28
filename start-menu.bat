@echo off
title BibliotecaPedroMJ - Ambientes de Execução
color 0A
cls
echo ==========================================
echo  BibliotecaPedroMJ - Menu de Ambientes
echo ==========================================
echo.
echo   1 - Development Angular + Spring (Test)
echo      Angular dev + Spring Boot com perfil 'test' (H2)
echo.
echo   2 - Production Complete
echo      Angular prod + Spring Boot com perfil 'prod' (PG)
echo.
echo   3 - Tests Only (Spring only)
echo      Spring Boot com perfil 'test' (H2 + console H2)
echo.
echo   4 - Compile Angular Only
echo      Build Angular dev ou prod (só compila)
echo.
echo   5 - Sair
echo.
set /p opcao="Escolha 1-5: "
echo.

if "%opcao%"=="1" goto dev
if "%opcao%"=="2" goto prod
if "%opcao%"=="3" goto test
if "%opcao%"=="4" goto compile
if "%opcao%"=="5" exit /b 0
echo Opção inválida! Digite 1-5.
timeout /t 2 >nul
goto menu

:dev
echo.
echo Iniciando DESENVOLVIMENTO (Angular + Spring Test)...
echo.
echo 1️⃣ Compilando Angular development...
cd /d "%~dp0frontend"
npx ng build --configuration development
if %errorlevel% neq 0 (
    echo ERRO na compilação Angular!
    pause
    goto menu
)
echo.
echo 2️⃣ Iniciando Spring Boot perfil 'test'...
cd /d "%~dp0"
call .\mvnw spring-boot:run -Dspring.profiles.active=test
if %errorlevel% neq 0 (
    echo ERRO ao iniciar Spring!
    pause
    goto menu
)
goto menu

:prod
echo.
echo Iniciando PRODUCAO Completa (Angular prod + Spring prod)...
echo.
echo 1️⃣ Compilando Angular production...
cd /d "%~dp0frontend"
npx ng build --configuration production
if %errorlevel% neq 0 (
    echo ERRO na compilação Angular!
    pause
    goto menu
)
echo.
echo 2️⃣ Iniciando Spring Boot perfil 'prod'...
cd /d "%~dp0"
call .\mvnw spring-boot:run -Dspring.profiles.active=prod
if %errorlevel% neq 0 (
    echo ERRO ao iniciar Spring!
    pause
    goto menu
)
goto menu

:test
echo.
echo Iniciando APenas o Spring no perfil TESTE...
echo.
echo Spring Boot com perfil 'test' (H2 + console H2).
echo Console: http://localhost:8080/h2-console
echo.
cd /d "%~dp0"
call .\mvnw spring-boot:run -Dspring.profiles.active=test
if %errorlevel% neq 0 (
    echo ERRO ao iniciar Spring!
    pause
    goto menu
)
goto menu

:compile
echo.
echo Compilar Angular Apenas
echo.
echo Escolha o tipo:
echo.
echo   1 - Build development (ng build --configuration development)
echo   2 - Build production      (ng build --configuration production)
echo   3 - Cancelar
echo.
set /p build="Digite 1, 2 ou 3: "
if "%build%"=="1" (
    echo.
    echo Compilando Angular development...
    cd /d "%~dp0frontend"
    npx ng build --configuration development
    if %errorlevel% neq 0 (
        echo ERRO!
    ) else (
        echo Compilado com sucesso!
    )
) else if "%build%"=="2" (
    echo.
    echo Compilando Angular production...
    cd /d "%~dp0frontend"
    npx ng build --configuration production
    if %errorlevel% neq 0 (
        echo ERRO!
    ) else (
        echo Compilado com sucesso!
    )
) else if "%build%"=="3" (
    echo.
    echo Cancelado.
) else (
    echo Opção inválida!
)
pause
goto menu

:menu
echo.
echo ==========================================
echo  BibliotecaPedroMJ - Menu de Ambientes
echo ==========================================
echo.
echo   1 - Development Angular + Spring (Test)
echo      Angular dev + Spring Boot com perfil 'test' (H2)
echo.
echo   2 - Production Complete
echo      Angular prod + Spring Boot com perfil 'prod' (PG)
echo.
echo   3 - Tests Only (Spring only)
echo      Spring Boot com perfil 'test' (H2 + console H2)
echo.
echo   4 - Compile Angular Only
echo      Build Angular dev ou prod (só compila)
echo.
echo   5 - Sair
echo.
set /p opcao="Escolha 1-5: "
echo.
if "%opcao%"=="1" goto dev
if "%opcao%"=="2" goto prod
if "%opcao%"=="3" goto test
if "%opcao%"=="4" goto compile
if "%opcao%"=="5" exit /b 0
echo Opção inválida! Digite 1-5.
timeout /t 2 >nul
goto menu
