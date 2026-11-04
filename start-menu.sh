#!/usr/bin/env bash
# =============================================================================
# BibliotecaPedroMJ - Menu unico de ambientes
#
#   ./start-menu.sh
#
# Roda em Linux, macOS e no Git Bash do Windows.
#
#   1 - Modo testes ........... Angular dev + jar na 8080, sem seguranca, H2
#   2 - Simulacao de PROD ..... Angular prod + jar na 8080, Postgres/MinIO Docker
#   3 - Modo PROD ............. reservado: ainda nao existe banco de producao
#   4 - Testes unitarios ...... mvnw test (backend) + ng test (frontend)
#   5 - Somente compilar ...... Angular + Spring Boot, sem iniciar nada
#   6 - Sair
# =============================================================================

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
STATIC_DIR="$ROOT_DIR/src/main/resources/static"
COMPOSE_FILE="$ROOT_DIR/src/main/resources/docker-compose.yml"
PORTA=8080
PROBLEMAS=0

# -----------------------------------------------------------------------------
# Ambiente
# -----------------------------------------------------------------------------
SO="linux"
case "$(uname -s)" in
    MINGW* | MSYS* | CYGWIN*) SO="windows" ;;
    Darwin*)                  SO="macos" ;;
esac

if [ -f "$ROOT_DIR/mvnw" ]; then
    [ -x "$ROOT_DIR/mvnw" ] || chmod +x "$ROOT_DIR/mvnw" 2>/dev/null
    MVN_CMD="$ROOT_DIR/mvnw"
else
    MVN_CMD="mvn"
fi

if [ -t 1 ]; then
    NEG=$'\033[1m'; AZUL=$'\033[36m'; VERDE=$'\033[32m'
    AMAR=$'\033[33m'; VERM=$'\033[31m'; RESET=$'\033[0m'
else
    NEG=''; AZUL=''; VERDE=''; AMAR=''; VERM=''; RESET=''
fi

# -----------------------------------------------------------------------------
# Helpers
# -----------------------------------------------------------------------------
tem() { command -v "$1" >/dev/null 2>&1; }

secao() { printf '\n%s\n' "${NEG}${AZUL}== $* ==${RESET}"; }
ok()    { printf '   %s[ok]%s     %s\n' "$VERDE" "$RESET" "$1"; }
aviso() { printf '   %s[aviso]%s  %s\n' "$AMAR" "$RESET" "$1"; }
falha() { printf '   %s[falha]%s  %s\n' "$VERM" "$RESET" "$1"; }

pausa() {
    printf '\n'
    read -r -p "Pressione Enter para voltar ao menu... " _resp || true
}

confirma() {
    local resposta
    read -r -p "$1 (s/N): " resposta || resposta="n"
    [[ "$resposta" =~ ^[sSyY] ]]
}

# Carrega um .env sem passar por eval: valores com ${...} ficam literais e quem
# resolve a referencia e o proprio Spring, igual ao que os scripts .ps1 faziam.
# O $2 opcional filtra: exporta apenas as chaves que comecam com aquele prefixo.
carregar_env() {
    local arquivo="$1"
    local filtro="${2:-}"
    if [ ! -f "$arquivo" ]; then
        falha "Arquivo de ambiente nao encontrado: $arquivo"
        return 1
    fi

    if [ -z "$filtro" ]; then
        secao "Variaveis de $(basename "$arquivo")"
    else
        secao "Variaveis de $(basename "$arquivo") comecando por ${filtro}"
    fi

    local linha chave valor
    while IFS= read -r linha || [ -n "$linha" ]; do
        linha="${linha%$'\r'}"
        case "$linha" in '' | '#'*) continue ;; esac
        [[ "$linha" != *"="* ]] && continue

        chave="${linha%%=*}"
        valor="${linha#*=}"
        chave="${chave//[[:space:]]/}"
        [ -z "$chave" ] && continue
        if [ -n "$filtro" ] && [[ "$chave" != "$filtro"* ]]; then continue; fi

        while [[ "$valor" == " "* ]]; do valor="${valor# }"; done
        while [[ "$valor" == *" " ]]; do valor="${valor% }"; done
        if [[ "$valor" == \"*\" || "$valor" == \'*\' ]]; then
            valor="${valor:1:${#valor}-2}"
        fi

        export "$chave=$valor"
        printf '   %s=***\n' "$chave"
    done < "$arquivo"
}

versao_java() {
    local bruto
    bruto="$(java -version 2>&1 | sed -n '1s/.*version "\([^"]*\)".*/\1/p')"
    case "$bruto" in
        1.*) printf '%s\n' "$(printf '%s' "${bruto#1.}" | cut -d. -f1)" ;;
        *)   printf '%s\n' "${bruto%%.*}" ;;
    esac
}

porta_em_uso() {
    case "$SO" in
        windows) netstat -ano 2>/dev/null | grep -E ":$PORTA[[:space:]]" | grep -qi listen ;;
        macos)   lsof -nP -iTCP:"$PORTA" -sTCP:LISTEN >/dev/null 2>&1 ;;
        *)
            if tem ss; then
                ss -ltn 2>/dev/null | grep -qE ":$PORTA[[:space:]]"
            elif tem lsof; then
                lsof -nP -iTCP:"$PORTA" -sTCP:LISTEN >/dev/null 2>&1
            else
                return 1
            fi
            ;;
    esac
}

# -----------------------------------------------------------------------------
# Checagem de dependencias
# -----------------------------------------------------------------------------
# $1 = "docker" quando o modo escolhido nao sobrevive sem os containers.
verificar_dependencias() {
    local docker_obrigatorio="${1:-opcional}"

    PROBLEMAS=0
    secao "Dependencias"

    if tem java; then
        local jv; jv="$(versao_java)"
        if [ "${jv:-0}" -ge 21 ] 2>/dev/null; then
            ok "Java $jv (o projeto alvo e Java 21)"
        else
            falha "Java $jv; o projeto pede 21 ou mais"
            PROBLEMAS=$((PROBLEMAS + 1))
        fi
    else
        falha "Java nao esta no PATH"
        PROBLEMAS=$((PROBLEMAS + 1))
    fi

    if tem node; then
        local nv; nv="$(node -v)"
        local nmajor="${nv#v}"; nmajor="${nmajor%%.*}"
        if [ "${nmajor:-0}" -ge 20 ] 2>/dev/null; then
            ok "Node $nv"
        else
            falha "Node $nv; o Angular 18 pede 20 ou mais"
            PROBLEMAS=$((PROBLEMAS + 1))
        fi
    else
        falha "Node nao esta no PATH"
        PROBLEMAS=$((PROBLEMAS + 1))
    fi

    if tem npm; then
        ok "npm $(npm -v)"
    else
        falha "npm nao esta no PATH"
        PROBLEMAS=$((PROBLEMAS + 1))
    fi

    if [ -f "$ROOT_DIR/mvnw" ]; then
        ok "Maven wrapper (./mvnw)"
    elif tem mvn; then
        aviso "Sem ./mvnw; usando o Maven global"
    else
        falha "Nem ./mvnw nem mvn encontrados"
        PROBLEMAS=$((PROBLEMAS + 1))
    fi

    if tem docker; then
        if docker info >/dev/null 2>&1; then
            ok "Docker $(docker --version 2>/dev/null | cut -d' ' -f3 | tr -d ',')"
        elif [ "$docker_obrigatorio" = "docker" ]; then
            falha "Docker instalado, mas o daemon nao responde (Docker Desktop aberto?)"
            PROBLEMAS=$((PROBLEMAS + 1))
        else
            aviso "Docker parado; irrelevante aqui, mas travaria a simulacao de PROD"
        fi
    elif [ "$docker_obrigatorio" = "docker" ]; then
        falha "Docker nao esta instalado; a simulacao de PROD precisa dele"
        PROBLEMAS=$((PROBLEMAS + 1))
    else
        aviso "Docker ausente; so afeta a simulacao de PROD (opcao 2)"
    fi

    if porta_em_uso; then
        falha "Porta $PORTA ja esta escutando; libere antes de subir"
        PROBLEMAS=$((PROBLEMAS + 1))
    else
        ok "Porta $PORTA livre"
    fi

    if [ "$PROBLEMAS" -gt 0 ]; then
        printf '\n'
        falha "$PROBLEMAS pendencia(s) acima precisam ser resolvidas."
        return 1
    fi
    return 0
}

listar_imagens_compose() {
    [ -f "$COMPOSE_FILE" ] || return 0
    sed -n 's/^[[:space:]]*image:[[:space:]]*//p' "$COMPOSE_FILE" \
        | tr -d '"' | sed '/^[[:space:]]*$/d' | sort -u
}

garantir_infra_docker() {
    secao "Infraestrutura do docker-compose.yml"

    local faltando=0 imagem
    while IFS= read -r imagem; do
        [ -n "$imagem" ] || continue
        if docker image inspect "$imagem" >/dev/null 2>&1; then
            ok "imagem $imagem"
        else
            falha "imagem $imagem ainda nao foi baixada"
            faltando=$((faltando + 1))
        fi
    done < <(listar_imagens_compose)

    if [ "$faltando" -gt 0 ]; then
        if confirma "Baixar as $faltando imagem(ns) que faltam"; then
            docker compose -f "$COMPOSE_FILE" pull || {
                falha "docker compose pull falhou."
                return 1
            }
        else
            falha "Sem as imagens a simulacao nao sobe."
            return 1
        fi
    fi

    if docker ps --format '{{.Names}}' 2>/dev/null | grep -qx biblioteca-postgres; then
        ok "biblioteca-postgres rodando"
    else
        aviso "O Postgres de simulacao (biblioteca-postgres) nao esta rodando."
        if confirma "Subir os containers do docker-compose"; then
            docker compose -f "$COMPOSE_FILE" up -d || {
                falha "docker compose up falhou."
                return 1
            }
            ok "Containers de simulacao iniciados."
        else
            falha "O perfil prodsim nao sobe sem o Postgres."
            return 1
        fi
    fi
}

# -----------------------------------------------------------------------------
# Build
# -----------------------------------------------------------------------------
compilar_angular() {
    local config="$1"

    secao "Compilando Angular ($config)"
    cd "$FRONTEND_DIR" || return 1

    if [ ! -d node_modules ]; then
        aviso "node_modules ausente; rodando npm install..."
        npm install || { falha "npm install falhou."; cd "$ROOT_DIR" || return 1; return 1; }
    fi

    npx ng build --configuration "$config"
    local status=$?
    cd "$ROOT_DIR" || return 1

    if [ $status -ne 0 ]; then
        falha "Compilacao do Angular falhou."
        return 1
    fi
    if [ -z "$(ls -A "$STATIC_DIR" 2>/dev/null)" ]; then
        falha "$STATIC_DIR ficou vazio; o Spring nao tera o que servir."
        return 1
    fi
    ok "Angular publicado em src/main/resources/static"
}

compilar_backend() {
    secao "Compilando Spring Boot (jar, sem testes unitarios)"
    "$MVN_CMD" -DskipTests package || { falha "Build Maven falhou."; return 1; }
    ok "Jar gerado em target/"
}

localizar_jar() {
    local jar
    for jar in "$ROOT_DIR"/target/*.jar; do
        [ -f "$jar" ] || continue
        printf '%s\n' "$jar"
        return 0
    done
    return 1
}

iniciar_jar() {
    local jar
    jar="$(localizar_jar)" || { falha "Nenhum jar em target/; compile primeiro."; return 1; }

    secao "Spring Boot na porta $PORTA"
    printf '   jar    : %s\n' "$(basename "$jar")"
    printf '   perfil : %s\n' "${SPRING_PROFILES_ACTIVE:-padrao}"
    printf '   URL    : %s\n\n' "http://localhost:$PORTA"
    printf 'Ctrl+C encerra e volta ao menu.\n\n'

    java -jar "$jar" --server.port="$PORTA"
}

# -----------------------------------------------------------------------------
# Opcoes do menu
# -----------------------------------------------------------------------------
modo_teste() {
    secao "MODO TESTES - H2 em memoria, seguranca liberada"

    verificar_dependencias || { pausa; return; }
    carregar_env "$ROOT_DIR/.env.example" || { pausa; return; }

    export SPRING_PROFILES_ACTIVE="test"
    export APP_TESTE_METODO_SEGURANCA="false"

    # O .env.example e um template: so o que o application-test.yml nao resolve
    # sozinho (os placeholders do Google OAuth) precisa vir dele. O resto, que ele
    # traz como placeholder invalido, sai do caminho para o profile responder.
    unset SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD
    unset APP_ENCRYPTION_KEY

    aviso "Todas as paginas e endpoints ficam publicos: nada de expor esta porta."
    aviso "Os dados moram no H2 em memoria e somem quando o processo desce."

    compilar_angular development && compilar_backend && iniciar_jar
    printf '\n'
    ok "Console H2: http://localhost:$PORTA/h2-console/ (JDBC URL jdbc:h2:mem:testdb, usuario sa)"
    pausa
}

modo_simulacao_prod() {
    secao "SIMULACAO DE PROD - Postgres/MinIO do Docker, seguranca ativa"

    verificar_dependencias docker || { pausa; return; }
    carregar_env "$ROOT_DIR/.env.prodsim.example" || { pausa; return; }

    # O .env.prodsim.example traz um client Google de brinquedo: o Spring sobe, mas
    # o login real cai em invalid_client. As credenciais validas moram so no .env
    # desta maquina, entao ele e lido apenas pelo prefixo GOOGLE_ e nada mais.
    if [ -f "$ROOT_DIR/.env" ]; then
        carregar_env "$ROOT_DIR/.env" "GOOGLE_"
        ok "Login do Google real ativo; o resto do ambiente segue sendo simulacao."
    else
        aviso "Sem .env real: o client Google fica ficticio e o login vai falhar."
    fi

    export SPRING_PROFILES_ACTIVE="prodsim"
    unset APP_TESTE_METODO_SEGURANCA

    aviso "O banco e o volume do docker-compose, entao os dados persistem entre boots."

    garantir_infra_docker && compilar_angular production && compilar_backend && iniciar_jar
    pausa
}

modo_prod() {
    secao "MODO PROD"
    printf 'Nada iniciado.\n\n'
    printf 'O banco de producao ainda nao existe, entao nao ha o que apontar aqui.\n'
    printf 'Quando ele estiver de pe, o caminho e:\n'
    printf '   1. copiar .env.prod.example para .env.prod e preencher de verdade\n'
    printf '   2. gerar a chave de cifragem: openssl rand -base64 32\n'
    printf '   3. rodar as migracoes do schema (o profile prod usa ddl-auto: validate)\n'
    printf '   4. entao liberar esta opcao para rodar o jar com SPRING_PROFILES_ACTIVE=prod\n'
    printf '\nEnquanto isso a simulacao local (opcao 2) cobre o comportamento de prod.\n'
    pausa
}

localizar_navegador() {
    if [ -n "${CHROME_BIN:-}" ] && [ -f "${CHROME_BIN:-}" ]; then
        printf '%s\n' "$CHROME_BIN"
        return 0
    fi

    if [ "$SO" != "windows" ]; then
        local bin
        for bin in google-chrome google-chrome-stable chromium chromium-browser; do
            tem "$bin" && { command -v "$bin"; return 0; }
        done
        return 1
    fi

    local caminho
    for caminho in \
        "/c/Program Files/Google/Chrome/Application/chrome.exe" \
        "/c/Program Files (x86)/Google/Chrome/Application/chrome.exe" \
        "/c/Program Files/Microsoft/Edge/Application/msedge.exe" \
        "/c/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"; do
        if [ -f "$caminho" ]; then
            if tem cygpath; then cygpath -w "$caminho"; else printf '%s\n' "$caminho"; fi
            return 0
        fi
    done
    return 1
}

modo_testes_unitarios() {
    secao "TESTES UNITARIOS"

    verificar_dependencias || { pausa; return; }

    if confirma "Rodar os testes do backend (mvnw test)"; then
        secao "Backend"
        "$MVN_CMD" test
        [ $? -eq 0 ] && ok "Backend passou" || falha "Backend com falhas"
    fi

    if confirma "Rodar os testes do frontend (ng test)"; then
        secao "Frontend"
        local navegador
        navegador="$(localizar_navegador)"
        if [ -z "$navegador" ]; then
            falha "Nem Chrome nem Edge encontrados; o Karma precisa de um deles."
            aviso "Defina CHROME_BIN apontando para o executavel do navegador."
        else
            export CHROME_BIN="$navegador"
            ok "Navegador do Karma: $navegador"
            cd "$FRONTEND_DIR" || { pausa; return; }
            [ -d node_modules ] || npm install
            npx ng test --watch=false --browsers=ChromeHeadlessNoSandbox
            [ $? -eq 0 ] && ok "Frontend passou" || falha "Frontend com falhas"
            cd "$ROOT_DIR" || return
        fi
    fi

    pausa
}

modo_compilar() {
    secao "SOMENTE COMPILAR"

    verificar_dependencias || { pausa; return; }

    local config="production"
    printf 'Configuracao do Angular:\n'
    printf '   1 - development (source map, sem otimizacao)\n'
    printf '   2 - production  (hash nos arquivos, otimizado)\n'
    read -r -p "Escolha 1-2 [2]: " escolha
    [ "$escolha" = "1" ] && config="development"

    compilar_angular "$config" && compilar_backend
    printf '\n'
    aviso "Nada foi iniciado. Use a opcao 1 ou 2 do menu para rodar o jar."
    pausa
}

# -----------------------------------------------------------------------------
# Menu
# -----------------------------------------------------------------------------
mostrar_menu() {
    printf '\n'
    printf '%s\n' "${NEG}============================================================${RESET}"
    printf '%s\n' "${NEG}  BibliotecaPedroMJ - Ambientes${RESET}"
    printf '%s\n' "${NEG}============================================================${RESET}"
    printf '\n'
    printf '   1 - Modo TESTES\n'
    printf '       Angular dev -> Spring -> jar na 8080. H2, seguranca toda liberada.\n\n'
    printf '   2 - Simulacao de PROD\n'
    printf '       Angular prod -> Spring -> jar na 8080. Postgres/MinIO do Docker,\n'
    printf '       seguranca ativa, credenciais ficticias.\n\n'
    printf '   3 - Modo PROD\n'
    printf '       Reservado: o banco de producao ainda nao foi criado.\n\n'
    printf '   4 - Testes unitarios (frontend + backend)\n\n'
    printf '   5 - Somente compilar Angular e Spring Boot\n\n'
    printf '   6 - Sair\n\n'
}

while true; do
    mostrar_menu
    read -r -p "Escolha 1-6: " opcao || break
    case "$opcao" in
        1) modo_teste ;;
        2) modo_simulacao_prod ;;
        3) modo_prod ;;
        4) modo_testes_unitarios ;;
        5) modo_compilar ;;
        6) printf '\nAte mais!\n'; break ;;
        *) falha "Opcao invalida: $opcao"; pausa ;;
    esac
done
