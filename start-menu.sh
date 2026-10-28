#!/bin/bash
# Menu de ambientes para BibliotecaPedroMJ
# Funciona no Windows (Git Bash) e Linux/Mac
# Uso: ./start-menu.sh  (ou clique no arquivo)

echo "========================================"
echo "BibliotecaPedroMJ - Menu de Ambientes"
echo "========================================"
echo
echo "1 - Development Angular + Spring (Test)"
echo "   Angular dev + Spring Boot com perfil 'test' (H2)"
echo
echo "2 - Production Complete"
echo "   Angular prod + Spring Boot com perfil 'prod' (PG)"
echo
echo "3 - Tests Only (Spring only)"
echo "   Spring Boot com perfil 'test' (H2 + console H2)"
echo
echo "4 - Compile Angular Only"
echo "   Build Angular dev ou prod (só compila)"
echo
echo "5 - Sair"
echo
read -p "Escolha 1-5: " opcao
echo

if [ "$opcao" = "1" ]; then
    echo
    echo "Iniciando DESENVOLVIMENTO (Angular + Spring Test)..."
    echo
    echo "1️⃣ Compilando Angular development..."
    cd "$(dirname "$0")/frontend" || exit 1
    npx ng build --configuration development
    if [ $? -ne 0 ]; then
        echo "ERRO na compilação Angular!"
        read -p "Press Enter to continue..."
        exit 1
    fi
    echo
    echo "2️⃣ Iniciando Spring Boot perfil 'test'..."
    cd ..
    ./mvnw spring-boot:run -Dspring.profiles.active=test
    if [ $? -ne 0 ]; then
        echo "ERRO ao iniciar Spring!"
        read -p "Press Enter to continue..."
        exit 1
    fi
    exit 0

elif [ "$opcao" = "2" ]; then
    echo
    echo "Iniciando PRODUCAO Completa..."
    echo
    echo "1️⃣ Compilando Angular production..."
    cd "$(dirname "$0")/frontend" || exit 1
    npx ng build --configuration production
    if [ $? -ne 0 ]; then
        echo "ERRO na compilação Angular!"
        read -p "Press Enter to continue..."
        exit 1
    fi
    echo
    echo "2️⃣ Iniciando Spring Boot perfil 'prod'..."
    cd ..
    ./mvnw spring-boot:run -Dspring.profiles.active=prod
    if [ $? -ne 0 ]; then
        echo "ERRO ao iniciar Spring!"
        read -p "Press Enter to continue..."
        exit 1
    fi
    exit 0

elif [ "$opcao" = "3" ]; then
    echo
    echo "Iniciando APenas o Spring no perfil TESTE..."
    echo
    echo "Spring Boot com perfil 'test' (H2 + console H2)."
    echo "Console: http://localhost:8080/h2-console"
    echo
    cd ..
    ./mvnw spring-boot:run -Dspring.profiles.active=test
    if [ $? -ne 0 ]; then
        echo "ERRO ao iniciar Spring!"
        read -p "Press Enter to continue..."
        exit 1
    fi
    exit 0

elif [ "$opcao" = "4" ]; then
    echo
    echo "Compilar Angular Apenas"
    echo
    echo "Escolha o tipo de build:"
    echo
    echo "1 - Build development (ng build --configuration development)"
    echo "2 - Build production      (ng build --configuration production)"
    echo "3 - Cancel"
    read -p "Enter 1, 2 or 3: " buildopt
    echo

    if [ "$buildopt" = "1" ]; then
        echo
        echo "Compilando Angular development..."
        cd "$(dirname "$0")/frontend" || exit 1
        npx ng build --configuration development
        if [ $? -ne 0 ]; then
            echo "ERRO de compilação!"
        else
            echo "Compilação concluída!"
        fi
    elif [ "$buildopt" = "2" ]; then
        echo
        echo "Compilando Angular production..."
        cd "$(dirname "$0")/frontend" || exit 1
        npx ng build --configuration production
        if [ $? -ne 0 ]; then
            echo "ERRO de compilação!"
        else
            echo "Compilação concluída!"
        fi
    elif [ "$buildopt" = "3" ]; then
        echo
        echo "Cancelado pelo usuário."
    else
        echo "Opção inválida!"
    fi
    read -p "Press Enter to continue..."

elif [ "$opcao" = "5" ]; then
    echo
    echo "Até mais!"
    exit 0

else
    echo
    echo "Opção inválida! Digite 1-5."
    read -p "Press Enter to continue..."
    exit 1
fi
