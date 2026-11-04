@echo off
setlocal enabledelayedexpansion
title BibliotecaPedroMJ - Ambientes

rem ===========================================================================
rem BibliotecaPedroMJ - Menu unico de ambientes (Windows)
rem
rem   De dois cliques em start-menu.bat, ou rode no cmd:  start-menu.bat
rem
rem   1 - Modo testes ........... Angular dev + jar na 8080, sem seguranca, H2
rem   2 - Simulacao de PROD ..... Angular prod + jar na 8080, Postgres/MinIO Docker
rem   3 - Modo PROD ............. reservado: ainda nao existe banco de producao
rem   4 - Testes unitarios ...... mvnw test (backend) + ng test (frontend)
rem   5 - Somente compilar ...... Angular + Spring Boot, sem iniciar nada
rem   6 - Sair
rem
rem O cmd abandona as linhas seguintes quando ha um "call :rotina" dentro de um
rem bloco entre parenteses, entao o fluxo aqui e todo plano, com goto e labels.
rem ===========================================================================

set "ROOT=%~dp0"
if "%ROOT:~-1%"=="\" set "ROOT=%ROOT:~0,-1%"
set "FRONTEND=%ROOT%\frontend"
set "STATIC=%ROOT%\src\main\resources\static"
set "COMPOSE=%ROOT%\src\main\resources\docker-compose.yml"
set "PORTA=8080"
set "PROBLEMAS=0"

if exist "%ROOT%\mvnw.cmd" (
  set "MVN_CMD=%ROOT%\mvnw.cmd"
) else (
  set "MVN_CMD=mvn"
)

:menu
cls
echo ============================================================
echo   BibliotecaPedroMJ - Ambientes
echo ============================================================
echo.
echo   1 - Modo TESTES
echo       Angular dev, Spring, jar na 8080. H2 e seguranca toda liberada.
echo.
echo   2 - Simulacao de PROD
echo       Angular prod, Spring, jar na 8080. Postgres e MinIO do Docker,
echo       seguranca ativa, credenciais ficticias.
echo.
echo   3 - Modo PROD
echo       Reservado: o banco de producao ainda nao foi criado.
echo.
echo   4 - Testes unitarios - frontend e backend
echo.
echo   5 - Somente compilar Angular e Spring Boot
echo.
echo   6 - Sair
echo.
set "OPCAO="
rem Sem teclado disponivel (stdin fechado) o set /p devolve erro 1: sair em vez
rem de voltar ao menu para sempre.
set /p OPCAO="Escolha 1-6: " || goto :sair

if "%OPCAO%"=="1" goto :modo_teste
if "%OPCAO%"=="2" goto :modo_simulacao
if "%OPCAO%"=="3" goto :modo_prod
if "%OPCAO%"=="4" goto :modo_testes_unitarios
if "%OPCAO%"=="5" goto :modo_compilar
if "%OPCAO%"=="6" goto :sair
echo    [falha]  Opcao invalida: %OPCAO%
call :pausa
goto :menu

rem ---------------------------------------------------------------------------
rem Opcoes
rem ---------------------------------------------------------------------------
:modo_teste
echo.
echo == MODO TESTES - H2 em memoria, seguranca liberada ==
call :verificar_dependencias ""
if not "!PROBLEMAS!"=="0" goto :modo_teste_fim

call :carregar_env "%ROOT%\.env.example"
if errorlevel 1 goto :modo_teste_fim

set "SPRING_PROFILES_ACTIVE=test"
set "APP_TESTE_METODO_SEGURANCA=false"

rem O .env.example e um template: so o que o application-test.yml nao resolve
rem sozinho (os placeholders do Google OAuth) precisa vir dele. O resto, que ele
rem traz como placeholder invalido, sai do caminho para o profile responder.
set "SPRING_DATASOURCE_URL="
set "SPRING_DATASOURCE_USERNAME="
set "SPRING_DATASOURCE_PASSWORD="
set "APP_ENCRYPTION_KEY="

echo    [aviso]  Todas as paginas e endpoints ficam publicos: nada de expor esta porta.
echo    [aviso]  Os dados moram no H2 em memoria e somem quando o processo desce.

call :compilar_angular development
if errorlevel 1 goto :modo_teste_fim
call :compilar_backend
if errorlevel 1 goto :modo_teste_fim
call :iniciar_jar
echo    [ok]     Console H2: http://localhost:%PORTA%/h2-console/ - JDBC url jdbc:h2:mem:testdb, usuario sa

:modo_teste_fim
call :pausa
goto :menu

:modo_simulacao
echo.
echo == SIMULACAO DE PROD - Postgres e MinIO do Docker, seguranca ativa ==
call :verificar_dependencias "docker"
if not "!PROBLEMAS!"=="0" goto :modo_simulacao_fim

call :carregar_env "%ROOT%\.env.prodsim.example"
if errorlevel 1 goto :modo_simulacao_fim

rem O .env.prodsim.example traz um client Google de brinquedo: o Spring sobe, mas
rem o login real cai em invalid_client. As credenciais validas moram so no .env
rem desta maquina, entao ele e lido apenas pelo prefixo GOOGLE_ e nada mais.
if exist "%ROOT%\.env" goto :modo_sim_google
echo    [aviso]  Sem .env real: o client Google fica ficticio e o login vai falhar.
goto :modo_sim_avisos

:modo_sim_google
call :carregar_env "%ROOT%\.env" "GOOGLE_"
echo    [ok]     Login do Google real ativo; o resto do ambiente segue sendo simulacao.

:modo_sim_avisos
set "SPRING_PROFILES_ACTIVE=prodsim"
set "APP_TESTE_METODO_SEGURANCA="

echo    [aviso]  O banco e o volume do docker-compose, entao os dados persistem entre boots.

call :garantir_infra_docker
if errorlevel 1 goto :modo_simulacao_fim
call :compilar_angular production
if errorlevel 1 goto :modo_simulacao_fim
call :compilar_backend
if errorlevel 1 goto :modo_simulacao_fim
call :iniciar_jar

:modo_simulacao_fim
call :pausa
goto :menu

:modo_prod
echo.
echo == MODO PROD ==
echo Nada iniciado.
echo.
echo O banco de producao ainda nao existe, entao nao ha o que apontar aqui.
echo Quando ele estiver de pe, o caminho e:
echo    1. copiar .env.prod.example para .env.prod e preencher de verdade
echo    2. gerar a chave de cifragem: openssl rand -base64 32
echo    3. rodar as migracoes do schema - o profile prod usa ddl-auto validate
echo    4. liberar esta opcao para rodar o jar com SPRING_PROFILES_ACTIVE=prod
echo.
echo Enquanto isso a simulacao local, opcao 2, cobre o comportamento de prod.
call :pausa
goto :menu

:modo_testes_unitarios
echo.
echo == TESTES UNITARIOS ==
call :verificar_dependencias ""
if not "!PROBLEMAS!"=="0" goto :modo_testes_fim

set "RESP="
set /p RESP="Rodar os testes do backend, mvnw test? (s/N): "
if /i not "!RESP!"=="s" goto :modo_testes_front
echo.
echo == Backend ==
pushd "%ROOT%"
call "%MVN_CMD%" test
set "STATUS=!ERRORLEVEL!"
popd
if "!STATUS!"=="0" (echo    [ok]     Backend passou) else (echo    [falha]  Backend com falhas)

:modo_testes_front
set "RESP="
set /p RESP="Rodar os testes do frontend, ng test? (s/N): "
if /i not "!RESP!"=="s" goto :modo_testes_fim
echo.
echo == Frontend ==
call :achar_navegador
if defined CHROME_BIN goto :modo_testes_navegador_ok
echo    [falha]  Nem Chrome nem Edge encontrados; o Karma precisa de um deles.
echo    [aviso]  Defina CHROME_BIN apontando para o executavel do navegador.
goto :modo_testes_fim

:modo_testes_navegador_ok
echo    [ok]     Navegador do Karma: !CHROME_BIN!
pushd "%FRONTEND%"
if not exist node_modules call npm install
call npx ng test --watch=false --browsers=ChromeHeadlessNoSandbox
set "STATUS=!ERRORLEVEL!"
popd
if "!STATUS!"=="0" (echo    [ok]     Frontend passou) else (echo    [falha]  Frontend com falhas)

:modo_testes_fim
call :pausa
goto :menu

:modo_compilar
echo.
echo == SOMENTE COMPILAR ==
call :verificar_dependencias ""
if not "!PROBLEMAS!"=="0" goto :modo_compilar_fim

echo Configuracao do Angular:
echo    1 - development - source map, sem otimizacao
echo    2 - production  - hash nos arquivos, otimizado
set "BUILD_CFG=production"
set "RESP="
set /p RESP="Escolha 1-2 [2]: "
if "%RESP%"=="1" set "BUILD_CFG=development"

call :compilar_angular !BUILD_CFG!
if errorlevel 1 goto :modo_compilar_fim
call :compilar_backend
if errorlevel 1 goto :modo_compilar_fim
echo.
echo    [aviso]  Nada foi iniciado. Use a opcao 1 ou 2 do menu para rodar o jar.

:modo_compilar_fim
call :pausa
goto :menu

:sair
echo.
echo Ate mais!
endlocal
exit /b 0

rem ---------------------------------------------------------------------------
rem Sub-rotinas
rem ---------------------------------------------------------------------------
:pausa
echo.
set "DUMMY="
set /p DUMMY=Pressione Enter para voltar ao menu...
exit /b 0

rem Carrega um .env sem eval: valores com ${...} ficam literais e quem resolve
rem a referencia e o proprio Spring, igual ao comportamento dos scripts antigos.
rem %2 opcional filtra: exporta apenas as chaves que comecam com aquele prefixo.
:carregar_env
if not exist "%~1" (
  echo    [falha]  Arquivo de ambiente nao encontrado: %~1
  exit /b 1
)
echo.
if not "%~2"=="" goto :carregar_env_filtrado
echo == Variaveis de %~nx1 ==
for /f "usebackq eol=# tokens=1,* delims==" %%K in ("%~1") do (
  if not "%%L"=="" set "%%K=%%L"
)
goto :carregar_env_fim

:carregar_env_filtrado
echo == Variaveis de %~nx1 comecando por %~2 ==
for /f "eol=# tokens=1,* delims==" %%K in ('findstr /b /r "%~2.*=" "%~1" 2^>nul') do (
  if not "%%L"=="" set "%%K=%%L"
)

:carregar_env_fim
echo    [ok]     variaveis exportadas para o processo
exit /b 0

:verificar_dependencias
set "PROBLEMAS=0"
echo.
echo == Dependencias ==

rem --- Java
set "JVER="
for /f "tokens=3" %%V in ('java -version 2^>^&1 ^| findstr /i /c:"version"') do if not defined JVER set "JVER=%%~V"
if defined JVER goto :dep_java_presente
echo    [falha]  Java nao esta no PATH
set /a PROBLEMAS+=1
goto :dep_fim_java

:dep_java_presente
for /f "tokens=1,2 delims=." %%M in ("!JVER!") do (
  set "JMAJ=%%M"
  set "JMIN=%%N"
)
if "!JMAJ!"=="1" set "JMAJ=!JMIN!"
if !JMAJ! LSS 21 goto :dep_java_velho
echo    [ok]     Java !JVER! - o projeto alvo e Java 21
goto :dep_fim_java

:dep_java_velho
echo    [falha]  Java !JVER! - o projeto pede 21 ou mais
set /a PROBLEMAS+=1

:dep_fim_java

rem --- Node
set "NVER="
for /f "delims=" %%V in ('node -v 2^>nul') do if not defined NVER set "NVER=%%V"
if defined NVER goto :dep_node_presente
echo    [falha]  Node nao esta no PATH
set /a PROBLEMAS+=1
goto :dep_fim_node

:dep_node_presente
set "NMAJ=!NVER:v=!"
for /f "tokens=1 delims=." %%M in ("!NMAJ!") do set "NMAJ=%%M"
if !NMAJ! LSS 20 goto :dep_node_velho
echo    [ok]     Node !NVER!
goto :dep_fim_node

:dep_node_velho
echo    [falha]  Node !NVER! - o Angular 18 pede 20 ou mais
set /a PROBLEMAS+=1

:dep_fim_node

rem --- npm
where npm >nul 2>nul
if errorlevel 1 goto :dep_npm_falta
set "PMVER="
for /f "delims=" %%V in ('npm -v 2^>nul') do if not defined PMVER set "PMVER=%%V"
echo    [ok]     npm !PMVER!
goto :dep_fim_npm

:dep_npm_falta
echo    [falha]  npm nao esta no PATH
set /a PROBLEMAS+=1

:dep_fim_npm

rem --- Maven
if exist "%ROOT%\mvnw.cmd" goto :dep_maven_wrapper
where mvn >nul 2>nul
if errorlevel 1 goto :dep_maven_falta
echo    [aviso]  Sem mvnw.cmd; usando o Maven global
goto :dep_fim_maven

:dep_maven_wrapper
echo    [ok]     Maven wrapper mvnw.cmd
goto :dep_fim_maven

:dep_maven_falta
echo    [falha]  Nem mvnw.cmd nem mvn encontrados
set /a PROBLEMAS+=1

:dep_fim_maven

rem --- Docker
where docker >nul 2>nul
if errorlevel 1 goto :dep_docker_ausente
docker info >nul 2>nul
if errorlevel 1 goto :dep_docker_parado
set "DVER="
for /f "tokens=3 delims=, " %%V in ('docker --version 2^>nul') do if not defined DVER set "DVER=%%V"
echo    [ok]     Docker !DVER!
goto :dep_fim_docker

:dep_docker_parado
if not "%~1"=="docker" goto :dep_docker_parado_aviso
echo    [falha]  Docker instalado, mas o daemon nao responde - Docker Desktop aberto?
set /a PROBLEMAS+=1
goto :dep_fim_docker

:dep_docker_parado_aviso
echo    [aviso]  Docker parado; irrelevante aqui, mas travaria a simulacao de PROD
goto :dep_fim_docker

:dep_docker_ausente
if "%~1"=="docker" goto :dep_docker_obrigatorio
echo    [aviso]  Docker ausente; so afeta a simulacao de PROD, opcao 2
goto :dep_fim_docker

:dep_docker_obrigatorio
echo    [falha]  Docker nao esta instalado; a simulacao de PROD precisa dele
set /a PROBLEMAS+=1

:dep_fim_docker

rem --- Porta
set "PORTA_OCUPADA=0"
netstat -ano | findstr /c:":%PORTA% " | findstr /i "LISTENING" >nul 2>nul
if errorlevel 1 goto :dep_porta_livre
echo    [falha]  Porta %PORTA% ja esta escutando; libere antes de subir
set /a PROBLEMAS+=1
goto :dep_fim_porta

:dep_porta_livre
echo    [ok]     Porta %PORTA% livre

:dep_fim_porta

if not "!PROBLEMAS!"=="0" (
  echo.
  echo    [falha]  !PROBLEMAS! pendencia^(s^) acima precisam ser resolvidas.
  exit /b 1
)
exit /b 0

:garantir_infra_docker
echo.
echo == Infraestrutura do docker-compose.yml ==
set "FALTAM_IMAGENS=0"
for /f "usebackq tokens=1,* delims=:" %%I in (`findstr /i /c:"image:" "%COMPOSE%"`) do (
  set "IMG=%%J"
  call :trim_esquerda
  docker image inspect "!IMG!" >nul 2>nul
  if errorlevel 1 (
    echo    [falha]  imagem !IMG! ainda nao foi baixada
    set /a FALTAM_IMAGENS+=1
  ) else (
    echo    [ok]     imagem !IMG!
  )
)

if not "!FALTAM_IMAGENS!"=="0" (
  set "RESP="
  set /p RESP="Baixar as !FALTAM_IMAGENS! imagem^(s^) que faltam? (s/N): "
  if /i not "!RESP!"=="s" (
    echo    [falha]  Sem as imagens a simulacao nao sobe.
    exit /b 1
  )
  docker compose -f "%COMPOSE%" pull
  if errorlevel 1 (
    echo    [falha]  docker compose pull falhou.
    exit /b 1
  )
)

set "PG_RODANDO=0"
docker ps --format "{{.Names}}" 2>nul | findstr /x /c:"biblioteca-postgres" >nul
if not errorlevel 1 set "PG_RODANDO=1"
if "!PG_RODANDO!"=="1" (
  echo    [ok]     biblioteca-postgres rodando
  exit /b 0
)

echo    [aviso]  O Postgres de simulacao (biblioteca-postgres) nao esta rodando.
set "RESP="
set /p RESP="Subir os containers do docker-compose? (s/N): "
if /i not "!RESP!"=="s" (
  echo    [falha]  O perfil prodsim nao sobe sem o Postgres.
  exit /b 1
)
docker compose -f "%COMPOSE%" up -d
if errorlevel 1 (
  echo    [falha]  docker compose up falhou.
  exit /b 1
)
echo    [ok]     Containers de simulacao iniciados.
exit /b 0

:trim_esquerda
if not defined IMG exit /b 0
if not "!IMG:~0,1!"==" " exit /b 0
set "IMG=!IMG:~1!"
goto :trim_esquerda

:compilar_angular
echo.
echo == Compilando Angular (%~1) ==
pushd "%FRONTEND%"
if exist node_modules goto :ang_deps_ok
echo    [aviso]  node_modules ausente; rodando npm install...
call npm install
if not errorlevel 1 goto :ang_deps_ok
popd
echo    [falha]  npm install falhou.
exit /b 1

:ang_deps_ok
call npx ng build --configuration %~1
set "STATUS=!ERRORLEVEL!"
popd
if not "!STATUS!"=="0" (
  echo    [falha]  Compilacao do Angular falhou.
  exit /b 1
)
dir /b "%STATIC%" 2>nul | findstr /r "." >nul
if errorlevel 1 (
  echo    [falha]  %STATIC% ficou vazio; o Spring nao tera o que servir.
  exit /b 1
)
echo    [ok]     Angular publicado em src\main\resources\static
exit /b 0

:compilar_backend
echo.
echo == Compilando Spring Boot - jar, sem testes unitarios ==
pushd "%ROOT%"
call "%MVN_CMD%" -DskipTests package
set "STATUS=!ERRORLEVEL!"
popd
if not "!STATUS!"=="0" (
  echo    [falha]  Build Maven falhou.
  exit /b 1
)
echo    [ok]     Jar gerado em target
exit /b 0

:localizar_jar
set "JAR_ENCONTRADO="
for %%F in ("%ROOT%\target\*.jar") do if not defined JAR_ENCONTRADO set "JAR_ENCONTRADO=%%~fF"
exit /b 0

:iniciar_jar
call :localizar_jar
if defined JAR_ENCONTRADO goto :iniciar_jar_segue
echo    [falha]  Nenhum jar em target; compile primeiro.
exit /b 1

:iniciar_jar_segue
for %%F in ("%JAR_ENCONTRADO%") do set "JAR_NOME=%%~nxF"
echo.
echo == Spring Boot na porta %PORTA% ==
echo    jar    : !JAR_NOME!
echo    perfil : !SPRING_PROFILES_ACTIVE!
echo    URL    : http://localhost:%PORTA%
echo.
echo Ctrl+C encerra e volta ao menu.
echo.
java -jar "%JAR_ENCONTRADO%" --server.port=%PORTA%
exit /b 0

rem O Karma so acha o Chrome sozinho; com Edge e preciso apontar o CHROME_BIN.
:achar_navegador
if defined CHROME_BIN exit /b 0
set "NAV="
for %%P in (
  "C:\Program Files\Google\Chrome\Application\chrome.exe"
  "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
  "C:\Program Files\Microsoft\Edge\Application\msedge.exe"
  "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
) do if not defined NAV if exist %%P set "NAV=%%~P"
if defined NAV set "CHROME_BIN=!NAV!"
exit /b 0
