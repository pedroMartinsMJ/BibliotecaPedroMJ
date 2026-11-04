# 📚 Biblioteca Pedro MJ

> A full-stack digital library for public-domain books: Spring Boot backend + Angular frontend, built around a privacy-by-design identity model where readers stay anonymous until the moment they publish.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Angular](https://img.shields.io/badge/Angular-18-red?logo=angular)](https://angular.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![MinIO](https://img.shields.io/badge/MinIO-Object%20Storage-red?logo=minio)](https://min.io/)
[![Tests](https://img.shields.io/badge/Tests-95%20passing-brightgreen)](#-testing)

---

## 🎯 Overview

**Biblioteca Pedro MJ** is a REST backend plus an Angular SPA for a public digital library of public-domain books (classics whose authors have been deceased for over 100 years). Anyone can browse the catalog and read PDFs/EPUBs in the browser; authenticated users can build a personal library and publish books.

What makes this project different from a typical CRUD portfolio piece is its **domain model**: the site is designed so a person never has to expose their real name to read or keep a private collection — and only has to verify one, under legal accountability, when they press *publish*.

---

## 🔐 Privacy & Identity Model (the core design)

This is the part I'm most proud of — a set of business rules implemented end-to-end, with tests for each:

- **Username is the public identity.** Registration requires no real name. `username` is unique and is the only identity ever shown in the catalog.
- **Civil identity is only required to publish.** Real name + Brazilian CPF (validated with the official check-digit algorithm, not a regex) are collected lazily: publishing without verified identity returns a `403` and the UI opens an inline identity panel before retrying.
- **Immutable identity history.** Every civil-identity confirmation is an **append-only row** (`insertable=true, updatable=false` in JPA terms): never updated, never deleted. There is always exactly one "current" identity per account, but the full trail is preserved.
- **Publication trail per book.** At publish time, the publisher's current identity is *photographed* into the book record. If illegal content is ever posted, the account owner can notify the authorities with a non-repudiable record.
- **Encryption at rest.** Names, CPFs and emails are encrypted with AES-256-GCM (random IV per write) via a JPA `AttributeConverter`. A database leak reveals nothing in plaintext.
- **Searchable encrypted email.** Since ciphertext can't be queried, emails are also indexed by a deterministic **HMAC blind index** — the account is encrypted *and* still lookup-able, with uniqueness enforced at the DB level.
- **Fail-fast key policy.** The app **refuses to start** in production if `APP_ENCRYPTION_KEY` is missing. A default key committed to the repository is treated as a bug, never as a fallback.
- **Deactivation hides, it never deletes.** Disabling an account makes its whole catalog invisible (soft-off), and reactivation restores it. Permanent deletion is a deliberate manual step for long-inactive data.
- **ISBN is not unique.** Two users may publish the same book (different file, cover, or source). A logged-in account publishing a classic is the *publisher*, not the author — the work's author is free text (`autorObra`).

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Angular SPA (frontend/)                 │
│   standalone components · lazy routes · auth interceptor    │
├─────────────────────────────────────────────────────────────┤
│                     Presentation layer                      │
│  AuthController · LivroController · UsuarioController       │
│  (+ typed exceptions → GlobalExceptionHandler)              │
├─────────────────────────────────────────────────────────────┤
│                       Domain / Service layer                │
│  LivroService · UsuarioService · MinioService               │
│  InspecionadorDeArquivo (magic-byte sniffing)               │
│  EscritaTransacionalDeLivro (transactional write bean)      │
├─────────────────────────────────────────────────────────────┤
│                      Persistence layer                      │
│  JPA repositories · EncryptingConverter (AES-GCM)           │
│  HashDeEmail (HMAC blind index) · manual mappers · DTOs     │
├─────────────────────────────────────────────────────────────┤
│                        Infrastructure                       │
│   PostgreSQL 15 · MinIO (S3-compatible) · Spring Security   │
│   JWT (RSA 2048) · OAuth2 (Google) · virtual threads        │
└─────────────────────────────────────────────────────────────┘
```

Engineering principles applied throughout:

- **Strict layering**: Controller → Service → Repository → Entity; entities are never exposed — DTOs (`*_CREATE` / `*_RESPONSE`) with manual mappers decouple the API from the model.
- **File upload is content-verified**: uploaded files are inspected by **magic bytes**, not by declared content type or extension — a `.txt` renamed to `.pdf` is rejected with `400`.
- **Storage-first writes with compensation**: binaries go to MinIO *outside* the DB transaction; if the transactional book write fails afterwards, the stored object is cleaned up (compensating action).
- **Self-invocation-safe transactions**: the transactional part of a book write lives in a dedicated `@Transactional` bean (`EscritaTransacionalDeLivro`), avoiding the Spring proxy self-call pitfall.
- **Performance hygiene**: `open-in-view: false`, `JOIN FETCH` + `@BatchSize` to avoid N+1 on paginated listings, Java 21 **virtual threads** enabled, separate endpoints for cover (cacheable 24 h) vs. file (streamed on demand).
- **Error contract**: 11 typed business exceptions mapped by a single `@RestControllerAdvice`, so clients get structured, predictable payloads.

---

## 🛠️ Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Framework | Spring Boot | 4.1.2 |
| Language | Java (virtual threads) | 21 (LTS) |
| Frontend | Angular (standalone components) | 18.2 |
| Database | PostgreSQL | 15 |
| Object Storage | MinIO (S3-compatible) | client 8.5.7 |
| Security | Spring Security + JWT RSA 2048 + OAuth2 (Google) | 4.x |
| Persistence | Spring Data JPA / Hibernate | 4.1.2 |
| Validation | Jakarta Bean Validation + custom validators (CPF) | — |
| Dev Database | H2 (in-memory, `test` profile) | — |
| Build | Maven wrapper + npm | — |

---

## 📡 API Endpoints

### Authentication (`/api/auth`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/auth/login` | Login with email **or** username, returns JWT (1 h) | ❌ |
| `POST` | `/api/auth/register` | Create account (username + email + password) | ❌ |

### Books (`/api/livros`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/livros` | Paged public catalog | ❌ |
| `GET` | `/api/livros/{id}` | Book details (404 for inactive books) | ❌ |
| `GET` | `/api/livros/publicador/{id}` | Books by publisher (paged) | ❌ |
| `GET` | `/api/livros/{id}/download` | Stream the file (PDF/EPUB) | ❌ |
| `GET` | `/api/livros/{id}/link` | Pre-signed URL (7 days) | ❌ |
| `GET` | `/api/livros/{id}/capa` | Cover image (cacheable) | ❌ |
| `POST` | `/api/livros` | Publish a book (multipart: file + optional cover) | ✅ |
| `PUT` | `/api/livros/{id}/arquivo` | Replace the file | ✅ |
| `PUT` | `/api/livros/{id}/capa` | Replace the cover | ✅ |
| `DELETE` | `/api/livros/{id}/capa` | Remove the cover | ✅ |
| `DELETE` | `/api/livros/{id}` | Delete a book (owner/admin) | ✅ |

### Users & identity (`/usuario`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/usuario/create` | Register (legacy entry point) | ❌ |
| `POST` | `/usuario/identidade` | Confirm civil identity (name + CPF, append-only) | ✅ |
| `GET` | `/usuario/tudo` | List all users with their books | 🔑 ADMIN |
| `PUT` | `/usuario/put/{id}` | Update a user | 🔑 ADMIN |
| `PUT` | `/usuario/reativar/{id}` | Reactivate a deactivated account | 🔑 ADMIN |
| `DELETE` | `/usuario/delete/{id}` | Deactivate an account (hides its catalog) | 🔑 ADMIN |

### Diagnostics (`/api/diagnostico`, ADMIN only)

`GET /threads` and `GET /simular-io` — thread-pool and I/O inspection under virtual threads.

---

## 🔒 Security

- **JWT signed with RSA 2048** (asymmetric): tokens are verifiable without sharing the private key; dev builds generate keys at runtime, prod injects them via environment.
- **OAuth2 login with Google** alongside credential login.
- **Role-based access**: `ROLE_ADMIN`, `ROLE_USER`, `ROLE_AUTOR`; method-level `@PreAuthorize` plus URL rules; CORS configured for the SPA origins.
- **BCrypt** password hashing; **AES-256-GCM** field encryption; **HMAC** blind indexes — no plaintext PII, no default keys, fail-fast startup.

---

## ✅ Validation rules

- Brazilian **CPF** via the official check-digit algorithm (rejects `111.111.111-11`-style sequences)
- **ISBN** format validation (duplicate ISBNs are *allowed* by design)
- Publication date not in the future; page count > 0
- File size limits: 50 MB book file, 5 MB cover
- Content-type verified from **file signature bytes**, never from the client's MIME header

---

## 🧪 Testing

**95 tests across 17 backend classes** — unit + Spring integration tests against real H2 and PostgreSQL containers:

```bash
./mvnw test
```

| Area | Coverage |
|------|----------|
| Identity & encryption | encrypted-at-rest names, searchable encrypted email (HMAC index), immutable publication trail |
| Collection visibility | deactivated account hides/reactivates its whole catalog |
| Books | file inspection (magic bytes), upload rules, ISBN duplicates allowed, pagination, deletion |
| Users | registration, updates, deactivation/reactivation, identity confirmation flow |
| Controllers | auth (email-or-username login), REST contracts, admin guards, diagnostics |
| Infra | PostgreSQL prod-simulation profile, Docker stack status |

The Angular app has its own Karma/Jasmine suite (services, guards, components) run with `npx ng test`.

---

## 🚀 Getting Started

### Prerequisites

- Java 21, Maven (wrapper included), Docker, Node.js 20+

### 1. Clone

```bash
git clone https://github.com/pedroMartinsMJ/BibliotecaPedroMJ.git
cd BibliotecaPedroMJ
```

### 2. Pick an environment

| Profile | Database | Storage | Use case |
|---------|----------|---------|----------|
| `test` (default) | In-memory H2 | In-memory fake | Zero-setup instant run |
| `prodsim` | Local PostgreSQL (Docker) | MinIO (Docker) | Full local simulation with seed data |
| `prod` | PostgreSQL | MinIO | Real deployment — **all env vars mandatory** |

Fastest demo (no infrastructure needed):

```bash
./mvnw spring-boot:run   # test profile: H2 + seeded data
```

Full simulation:

```bash
cd src/main/resources && docker compose up -d   # PostgreSQL + MinIO (+ pgAdmin/Adminer)
SPRING_PROFILES_ACTIVE=prodsim ./mvnw spring-boot:run
cd frontend && npm install && npm start          # Angular on :4200, proxied to :8080
```

`npm run build` outputs the SPA into `src/main/resources/static`, so Spring Boot can serve backend and frontend as one artifact.

Two interactive launchers cover every environment, one per OS family: `start-menu.sh` (Linux/macOS/Git Bash) and `start-menu.bat` (Windows). Both check the toolchain (Java 21+, Node 20+, npm, Maven, Docker and the compose images) before doing anything, then offer:

| Option | What it does |
|--------|--------------|
| 1 | Test mode: Angular dev build, jar on `:8080`, H2, **security fully open** for page and CSS work |
| 2 | Production simulation: Angular prod build, jar on `:8080`, PostgreSQL + MinIO from Docker, security on, seeded demo data |
| 3 | Production: reserved — no production database exists yet |
| 4 | Unit tests for backend (`mvnw test`) and frontend (`ng test`) |
| 5 | Compile Angular and Spring Boot only, run nothing |
| 6 | Exit |

Option 1 reads [`.env.example`](.env.example) and option 2 reads [`.env.prodsim.example`](.env.prodsim.example), so neither needs a real `.env`. Option 2 makes one exception: when a local [`.env`](.env.example) exists it imports **only** the `GOOGLE_*` keys from it, because a Google OAuth client cannot be simulated — every other variable stays a simulation placeholder. Environment variables are documented in [`.env.example`](.env.example).

---

## 📂 Project Structure

```
src/main/java/com/pedroMartinsMJ/bibliotecaPedroMJ/
├── entities/            Usuario, Identidade (append-only), Livro, BibliotecaPessoal, Role
├── repositories/        Spring Data JPA
├── services/            LivroService, UsuarioService, MinioService,
│                        InspecionadorDeArquivo, EscritaTransacionalDeLivro
├── controllers/         Auth, Livro, Usuario, Diagnostics, SPA forwarding
│   ├── DTOs/            *_CREATE / *_RESPONSE records
│   └── mappersManuais/  explicit entity ⇄ DTO mappers
├── security/            EncryptingConverter (AES-GCM), HashDeEmail (HMAC)
├── segurityConfig/      SecurityConfig, JWT service, OAuth2 wiring
├── validators/          CPF check-digit validator + annotations
├── tratamentoDeErros/   GlobalExceptionHandler + typed business exceptions
└── config/              MinIO config, profile-based seeders

frontend/                Angular 18 standalone SPA
src/main/resources/      application-{test,prodsim,prod}.yaml, docker-compose.yml
```

---

## 📝 Further Reading

- [`SPRING_BOOT_4X_MIGRATION.md`](SPRING_BOOT_4X_MIGRATION.md) — migrating this codebase to Spring Boot 4.x
- [`SPRING_SECURITY_4X_MIGRATION.md`](SPRING_SECURITY_4X_MIGRATION.md) — Spring Security 4.x API changes and pitfalls (e.g. `hasRole` prefix behavior)

---

## 👨‍💻 Author

**Pedro Martins**

- GitHub: [@pedroMartinsMJ](https://github.com/pedroMartinsMJ)
- Repository: [BibliotecaPedroMJ](https://github.com/pedroMartinsMJ/BibliotecaPedroMJ)
