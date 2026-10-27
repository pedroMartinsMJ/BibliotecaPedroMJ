# 📚 Biblioteca Pedro MJ

> Backend de uma biblioteca digital para livros de domínio público, desenvolvido com Spring Boot, focado em leitura e postagem de livros sem direitos autorais.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![MinIO](https://img.shields.io/badge/MinIO-Object%20Storage-red?logo=minio)](https://min.io/)
[![Tests](https://img.shields.io/badge/Tests-45%20passing-brightgreen)](https://github.com/pedroMartinsMJ/BibliotecaPedroMJ)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

## 🎯 Sobre o Projeto

A **Biblioteca Pedro MJ** é um backend RESTful completo para uma biblioteca digital de livros de domínio público. O sistema permite:

- **Catálogo público** de livros com capas e metadados
- **Leitura online** de PDFs e EPUBs diretamente no navegador
- **Postagem de livros** por autores autenticados
- **Biblioteca pessoal** do usuário (em desenvolvimento)
- **Autenticação** via JWT e OAuth2 (Google)

O projeto foi desenvolvido com foco em **boas práticas de engenharia de software**: separação de camadas, DTOs, validação customizada, segurança, testes unitários e documentação.

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                    Camada de Apresentação                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  ViewController  │  │  LivroController │  │  UsuarioController│   │
│  │  (Thymeleaf)     │  │  (REST API)      │  │  (REST API)       │   │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
├─────────────────────────────────────────────────────────────┤
│                    Camada de Serviço                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  LivroService   │  │  UsuarioService │  │  MinioService    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
├─────────────────────────────────────────────────────────────┤
│                    Camada de Persistência                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  LivroRepository│  │  UsuarioRepository│ │  RoleRepository  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
├─────────────────────────────────────────────────────────────┤
│                    Infraestrutura                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  PostgreSQL     │  │  MinIO (S3)     │  │  Spring Security │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### Princípios aplicados:
- **Separação de camadas**: Controller → Service → Repository → Entity
- **DTO Pattern**: Entidades nunca expostas diretamente na API
- **Mapper Pattern**: Conversão manual entre Entity e DTO
- **Validação customizada**: CPF brasileiro com algoritmo real de dígitos verificadores
- **Tratamento global de erros**: `@RestControllerAdvice` padroniza respostas

---

## 🛠️ Tecnologias

| Categoria | Tecnologia | Versão |
|-----------|-----------|--------|
| Framework | Spring Boot | 4.1.2 (LTS) |
| Linguagem | Java | 21 (LTS) |
| Banco de Dados | PostgreSQL | 15 |
| Object Storage | MinIO | 8.5.7 |
| Segurança | Spring Security + JWT (Nimbus) | 4.x |
| OAuth2 | Google Login | - |
| Template Engine | Thymeleaf | - |
| Build | Maven | 3.9.12 |
| Containerização | Docker Compose | - |
| Testes | JUnit 5 + Mockito | - |

---

## ✨ Funcionalidades

### 📖 Livros
- ✅ Listagem paginada de livros (20 itens/página)
- ✅ Busca por ID com autor carregado (JOIN FETCH - sem N+1)
- ✅ Busca por autor
- ✅ Upload de arquivo (PDF/EPUB, até 50MB)
- ✅ Upload de capa (JPEG/PNG/WebP, até 5MB)
- ✅ Download de arquivo (streaming)
- ✅ Download de capa (com cache de 24h)
- ✅ Geração de URL pré-assinada (7 dias)
- ✅ Deleção completa (arquivo + capa + metadados)
- ✅ Validação de ISBN duplicado

### 👤 Usuários
- ✅ Cadastro com validação de CPF brasileiro
- ✅ Criptografia de senha com BCrypt
- ✅ Atribuição automática de role `ROLE_USER`
- ✅ Validação de duplicidade (username, email, CPF, telefone)
---

## 🚀 Como Rodar o Projeto

### Pré-requisitos
- Java 21 (LTS)
- Maven 3.9+
- Docker e Docker Compose

### 1. Clone o repositório
```bash
git clone https://github.com/pedroMartinsMJ/BibliotecaPedroMJ.git
cd BibliotecaPedroMJ
```

### 2. Suba a infraestrutura (PostgreSQL + MinIO)
```bash
cd src/main/resources
docker compose up -d
```

### 3. Configure as variáveis de ambiente (opcional)
```bash
# Windows (PowerShell)
$env:DB_USERNAME="pedro"
$env:DB_PASSWORD="senha123"
$env:MINIO_ACCESS_KEY="minioadmin"
$env:MINIO_SECRET_KEY="minioadmin123"

# Linux/macOS
export DB_USERNAME=pedro
export DB_PASSWORD=senha123
export MINIO_ACCESS_KEY=minioadmin
export MINIO_SECRET_KEY=minioadmin123
```

### 4. Execute a aplicação
```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### 5. Acesse o MinIO Console
- URL: `http://localhost:9001`
---

## 🧪 Testes

```bash
# Rodar todos os testes
./mvnw test

# Rodar um teste específico
./mvnw test -Dtest=CPFValidatorTest
```

### Cobertura de testes
| Classe | Testes | Cobertura |
|--------|--------|-----------|
| `CPFValidatorTest` | 11 | Algoritmo de CPF, formatação, nulos, letras |
| `LivroControllerTest` | 3 | Endpoints REST (público, auth, criação) |
| `UsuarioServiceTest` | 10 | Cadastro, atualização, deleção, busca, senha |
| `ValidacaoUsuarioTest` | 9 | Duplicidade, validação de atualização |
| `LivroServiceTest` | 11 | Validação de arquivo, capa, ISBN, busca, deleção |
| `BibliotecaPedroMjApplicationTests` | 1 | Contexto da aplicação |
| **Total** | **45** | **BUILD SUCCESS** ✅ |

---

## 📡 Endpoints da API

### Livros (`/api/livros`)

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| `GET` | `/api/livros` | Listar livros (paginado) | ❌ |
| `GET` | `/api/livros/{id}` | Buscar livro por ID | ❌ |
| `GET` | `/api/livros/autor/{id}` | Buscar livros por autor (paginado) | ❌ |
| `GET` | `/api/livros/{id}/download` | Download do arquivo (PDF/EPUB) | ❌ |
| `GET` | `/api/livros/{id}/capa` | Download da capa (imagem) | ❌ |
| `GET` | `/api/livros/{id}/link` | Gerar URL pré-assinada (7 dias) | ❌ |
| `GET` | `/api/livros/{id}/capa/url` | Gerar URL da capa | ❌ |
| `POST` | `/api/livros` | Criar livro (multipart) | ✅ |
| `PUT` | `/api/livros/{id}/arquivo` | Atualizar arquivo | ✅ |
| `PUT` | `/api/livros/{id}/capa` | Atualizar capa | ✅ |
| `DELETE` | `/api/livros/{id}/capa` | Remover capa | ✅ |
| `DELETE` | `/api/livros/{id}` | Deletar livro | ✅ |

### Usuários (`/usuario`)

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| `POST` | `/usuario/create` | Criar usuário | ❌ |
| `GET` | `/usuario/tudo` | Listar todos os usuários | ✅ (Admin) |
| `PUT` | `/usuario/put/{id}` | Atualizar dados | ✅ |
| `DELETE` | `/usuario/delete/{id}` | Deletar usuário | ✅ |

### Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/authenticate` | Login (retorna JWT) |
| `GET` | `/login` | Página de login |
---

## 📁 Estrutura do Projeto

```
src/main/java/com/pedroMartinsMJ/bibliotecaPedroMJ/
├── BibliotecaPedroMjApplication.java
├── config/
│   ├── MinioConfig.java
│   └── inicializacaoTest.java
├── controllers/
│   ├── LivroController.java          # REST API de livros
│   ├── UsuarioController.java        # REST API de usuários + Web
│   ├── ViewController.java           # Páginas Thymeleaf
│   ├── DTOs/
│   │   ├── LivroDTO_CREATE.java      # DTO de criação (record)
│   │   ├── LivroDTO_RESPONSE.java    # DTO de resposta (record)
│   │   ├── UsuarioDTO_CREATE.java    # DTO de criação (record)
│   │   └── UsuarioDTO_RESPONSE.java  # DTO de resposta (record)
│   └── mappersManuais/
│       ├── LivroMapper.java          # Entity ↔ DTO
│       └── UsuarioMapper.java        # Entity ↔ DTO
├── entities/
│   ├── Usuario.java                  # Entidade de usuário
│   ├── Livro.java                    # Entidade de livro
│   ├── Role.java                     # Entidade de role
│   ├── BibliotecaPessoal.java        # Biblioteca do usuário
│   └── enums/
│       ├── FormatoArquivo.java       # PDF, EPUB
│       ├── StatusLeitura.java        # PENDENTE, LENDO, LIDO, ABANDONADO
│       └── TipoArquivo.java          # PDF, EPUB com contentType
├── repositorys/
│   ├── UsuarioRepository.java
│   ├── LivroRepository.java
│   ├── RoleRepository.java
│   └── BibliotecaPessoalRepository.java
├── segurityConfig/
│   ├── SecurityConfig.java           # Segurança para produção
│   ├── SecurityConfigTest.java       # Segurança para testes
│   ├── AuthenticationController.java
│   ├── JWT/
│   │   ├── AutheticationService.java
│   │   └── JwtService.java
│   └── UserDetails/
│       ├── CustomUserDetailsService.java
│       └── UserAuthenticated.java
├── services/
│   ├── LivroService.java             # Lógica de negócio de livros
│   ├── UsuarioService.java           # Lógica de negócio de usuários
│   └── MinioService.java             # Integração com MinIO
├── tratamentoDeErros/
│   ├── GlobalExceptionHandler.java   # Handler global de exceções
│   └── exceptions/
│       └── ValidationExceptionDeUsuario.java
└── validators/
    ├── CPFValidator.java             # Validador de CPF
    ├── ValidCPF.java                # Anotação customizada
    └── ValidacaoUsuario.java        # Validações de duplicidade
```

---

## 🎓 Decisões de Arquitetura

### 1. Endpoints separados para arquivo e capa
Arquivo (PDF/EPUB) e capa (imagem) são recursos diferentes com características distintas:
- **Tamanho**: Arquivo até 50MB, capa até 5MB
- **Cache**: Capa pode ser cacheada (24h), arquivo não
- **Uso**: Capa carrega em listas, arquivo só ao ler/baixar

### 2. Validação customizada de CPF
Implementação do algoritmo oficial brasileiro de dígitos verificadores, não apenas regex. Rejeita sequências inválidas (ex: 111.111.111-11).

### 3. JWT com RSA
Uso de chaves assimétricas (RSA 2048 bits) em vez de HMAC, permitindo verificação de token sem compartilhar a chave privada.

### 4. MinIO para arquivos
Separação de metadados (PostgreSQL) de binários (Object Storage), seguindo o padrão de armazenamento de objetos (S3-compatible).

### 5. Paginação com JOIN FETCH
Evita o problema N+1 (1 query para livros + N queries para autores) usando `JOIN FETCH` no repositório.

---

## 📝 Documentação Adicional

- [`SPRING_BOOT_4X_MIGRATION.md`](SPRING_BOOT_4X_MIGRATION.md) — Guia de migração para Spring Boot 4.x
- [`SPRING_SECURITY_4X_MIGRATION.md`](SPRING_SECURITY_4X_MIGRATION.md) — Mudanças do Spring Security 3.x/5.x para 4.x
- [`ARQUITETURA_ENDPOINTS.txt`](src/main/java/com/pedroMartinsMJ/bibliotecaPedroMJ/controllers/ARQUITETURA_ENDPOINTS.txt) — Decisão de arquitetura de endpoints

---

## 👨‍💻 Autor

**Pedro Martins MJ**

- GitHub: [@pedroMartinsMJ](https://github.com/pedroMartinsMJ)
- Repositório: [BibliotecaPedroMJ](https://github.com/pedroMartinsMJ/BibliotecaPedroMJ)

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
| `GET` | `/oauth2/authorization/google` | Login com Google |

### Páginas Web (Thymeleaf)

| URL | Descrição |
|-----|-----------|
| `/` | Página inicial |
| `/livros` | Catálogo público |
| `/livros/{id}` | Detalhe do livro |
| `/leitor/{id}` | Leitor de PDF/EPUB |
| `/login` | Login |
| `/autores/cadastro` | Cadastro de autor |
| `/dashboard` | Área autenticada |
| `/postar-livro` | Postar novo livro |
- Usuário: `minioadmin`
- Senha: `minioadmin123`

### 6. Acesse o Adminer (PostgreSQL)
- URL: `http://localhost:8085`
- Sistema: PostgreSQL
- Servidor: `postgres`
- Usuário: `pedro`
- Senha: `senha123`
- Banco: `biblioteca_db`
- ✅ Atualização de dados (sem alterar senha)
- ✅ Atualização de senha separada
- ✅ Deleção de usuário
- ✅ Busca por ID, username, email

### 🔐 Segurança
- ✅ Autenticação via JWT (RSA 2048 bits)
- ✅ OAuth2 Login (Google)
- ✅ Controle de acesso baseado em roles (`ROLE_ADMIN`, `ROLE_USER`)
- ✅ Endpoints públicos: leitura de livros
- ✅ Endpoints protegidos: criação, edição, deleção
- ✅ CORS configurado

### 📊 Validações
- ✅ CPF brasileiro (algoritmo real de dígitos verificadores)
- ✅ Email válido
- ✅ ISBN válido (formato internacional)
- ✅ Data de publicação não futura
- ✅ Número de páginas > 0
- ✅ Tamanho de arquivo (50MB) e capa (5MB)