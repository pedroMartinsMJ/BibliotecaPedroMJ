#!/bin/bash
# =============================================================================
# BibliotecaPedroMJ - Build Angular + Spring Boot (tudo em localhost:8080)
# =============================================================================
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"

echo "============================================================"
echo "  BibliotecaPedroMJ - Build & Run (Angular + Spring Boot)"
echo "============================================================"
echo ""

# -----------------------------------------------------------------------------
# 1. Verificar variáveis de ambiente obrigatórias
# -----------------------------------------------------------------------------
echo "[1/4] Verificando variáveis de ambiente..."
if [ -z "$GOOGLE_CLIENT_ID" ] || [ -z "$GOOGLE_CLIENT_SECRET" ]; then
  echo "⚠️  ATENÇÃO: GOOGLE_CLIENT_ID e/ou GOOGLE_CLIENT_SECRET não estão definidos."
  echo "   O login com Google não funcionará sem essas variáveis."
  echo "   Exporte-as antes de continuar:"
  echo "     export GOOGLE_CLIENT_ID=seu_client_id"
  echo "     export GOOGLE_CLIENT_SECRET=seu_client_secret"
  echo ""
  read -p "Deseja continuar mesmo assim? (s/N): " continuar
  if [[ ! "$continuar" =~ ^[sS]$ ]]; then
    echo "Cancelado pelo usuário."
    exit 1
  fi
fi

if [ -z "$SPRING_PROFILES_ACTIVE" ]; then
  export SPRING_PROFILES_ACTIVE=prodsim
  echo "   SPRING_PROFILES_ACTIVE não definido, usando padrão: prodsim"
fi

echo "   SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE"
echo "   GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID:0:20}..."  # mostra só o início por segurança
echo ""

# -----------------------------------------------------------------------------
# 2. Compilar Angular
# -----------------------------------------------------------------------------
echo "[2/4] Compilando Angular para produção..."
cd "$FRONTEND_DIR"

# Instalar dependências se node_modules não existir
if [ ! -d "node_modules" ]; then
  echo "   node_modules não encontrado, rodando npm install..."
  npm install
fi

# Compilar Angular (outputPath já está configurado para ../src/main/resources/static)
npx ng build --configuration production

if [ $? -ne 0 ]; then
  echo "❌ Erro ao compilar o Angular!"
  exit 1
fi

echo "   ✅ Angular compilado com sucesso em: $ROOT_DIR/src/main/resources/static"
echo ""

# -----------------------------------------------------------------------------
# 3. Rodar Spring Boot
# -----------------------------------------------------------------------------
echo "[3/4] Iniciando Spring Boot na porta 8080..."
cd "$ROOT_DIR"

# Verificar se o diretório static foi populado
if [ ! -d "src/main/resources/static" ] || [ -z "$(ls -A src/main/resources/static)" ]; then
  echo "❌ O diretório src/main/resources/static está vazio. A compilação do Angular pode ter falhado."
  exit 1
fi

echo "   ✅ Arquivos do Angular encontrados em src/main/resources/static"
echo "   🚀 Iniciando Spring Boot..."
echo ""

./mvnw spring-boot:run -Dspring.profiles.active="$SPRING_PROFILES_ACTIVE"

echo ""
echo "[4/4] Processo finalizado."