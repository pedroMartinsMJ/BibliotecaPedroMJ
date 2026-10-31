
## 🧭 Visão Geral do Projeto

Este é um projeto **Spring Boot 4.1.2 + Angular 18** (banco Mono-repo) que migrou de Thymeleaf→Angular, adicionou criptografia de dados sensíveis e sistemas de autenticação.
## ✅ O que foi concluído (versal)

### Fase 1 — Commit & Configuração
| Arquivo | Ação |
|---------|------|
| `.env` | Criado, ignorado por `.gitignore` |
| `.env.example` | Criado (versão versionada do `.env` para referencia) |
| `.gitignore` | Atualizado (adiciona `.env`, `node_modules`, `package-lock.json`) |

### Fase 2 — Criptografia de Dados Sensíveis ✅
| Arquivo | Ação |
|---------|------|
| `src/main/java/.../security/CryptoUtil.java` | **NOVO** — AES-256 GCM, gera chave Base64, `encrypt()` e `decrypt()` |
| `src/main/java/.../security/EncryptingConverter.java` | **NOVO** — JPA `AttributeConverter` para criptografar automaticamente o email |
| `src/main/java/.../entities/Usuario.java` | Removido `cpf` e `telefone`, adicionado `@Convert(converter = EncryptingConverter.class)` no `email` |
| `src/main/java/.../controllers/DTOs/UsuarioDTO_CREATE.java` | Removido campos `cpf` e `telefone` |
| `src/main/java/.../controllers/DTOs/UsuarioDTO_RESPONSE.java` | Removido campos `cpf` e `telefone` |
| `src/main/java/.../services/UsuarioService.java` | Removido referências a `cpf` e `telefone` |
| `src/main/java/.../repositorys/UsuarioRepository.java` | Removidos métodos `existsByCpf()` e `existsByTelefone()` |
| `src/main/java/.../validators/ValidacaoUsuario.java` | Removidas validações de `cpf` e `telefone` |
| `src/main/resources/application.yaml` | Adicionada seção `app.encryption.key` |
| `src/main/resources/application-test.yml` | Adicionada `app.encryption.key` para testes |
| `src/test/.../UsuarioServiceTest.java` | Reescrito sem referências a `cpf`/`telefone` |
| `src/test/.../ValidacaoUsuarioTest.java` | Reescrito sem referências a `cpf`/`telefone` |
| `src/config/inicializacaoTest.java` | Removidos `setCpf()` e `setTelefone()` |
| `src/tratamentoDeErros/GlobalExceptionHandler.java` | Removido tratamento de `uk_usuario_cpf` e mensagens `cpf` |

### Fase 3 — Autenticação (login/register) ✅
| Arquivo | Ação |
|---------|------|
| `src/main/java/.../controllers/AuthController.java` | **NOVO** — `/api/auth/login` e `/api/auth/register` |
| `src/main/java/.../controllers/DTOs/LoginRequest.java` | **NOVO** |
| `src/main/java/.../controllers/DTOs/LoginResponse.java` | **NOVO** |
| `src/main/java/.../controllers/DTOs/RegisterRequest.java` | **NOVO** |

### Configuração de Segurança
| Arquivo | Ação |
|---------|------|
| `src/main/java/.../segurityConfig/SecurityConfig.java` | Adicionado `daoAuthenticationProvider`, `AuthenticationManager` bean |
| `src/main/java/.../segurityConfig/SecurityConfigTest.java` | Adicionado `daoAuthenticationProvider`, `AuthenticationManager` |
## ⏳ O QUE FALTA / O QUE FALHOU

### Problema atual (bloqueante) ✅ RESOLVIDO
Os testes de integração (`LivroControllerTest`, `BibliotecaPedroMjApplicationTests`) falhavam ao tentar carregar o `ApplicationContext` no perfil `test` com erro:
> `AuthenticationManager` não pode ser criado

**Causa:** O `SecurityConfigTest` e `SecurityConfig` não expunham o bean `AuthenticationManager`, necessário para que o `AuthController` seja instanciado pelo Spring.

**Solução aplicada (30/10/2026):**
- Adicionado `CustomUserDetailsService` como dependência injetada nos construtores de `SecurityConfig` e `SecurityConfigTest`
- Adicionados os beans `daoAuthenticationProvider()` e `authenticationManager()` em ambas as classes
- Corrigida API do `DaoAuthenticationProvider` para Spring Security 7 (construtor recebe `UserDetailsService` diretamente)

### O que está rodando
- ✅ `./mvnw compile` — compila com sucesso
- ✅ `./mvnw test` (perfil `test`) — **46 testes, 0 erros** ✅ (inclui AuthControllerTest completo)
- ✅ `npm test` (Angular) — **33 testes, 0 erros** ✅ (Headless Chromium)
- ✅ `npm run build` (Angular) — **Build com sucesso em `src/main/resources/static`** ✅

---
### O que foi implementado recentemente:
1. **Conserto de Injeção no Spring Security**: `AuthenticationManager` e `DaoAuthenticationProvider` expostos corretamente no `SecurityConfig` e `SecurityConfigTest`.
2. **Correção de busca de usuário autenticado no login**: Mudança de `buscarPorEmail` para `buscarPorUsername` no `AuthController.login`, preservando a criptografia AES-GCM do e-mail.
3. **Tratamento de exceções de autenticação**: `GlobalExceptionHandler` agora captura `AuthenticationException` e `BadCredentialsException` retornando HTTP 401.
4. **Alinhamento dos DTOs Frontend/Backend**: `auth.model.ts`, `usuario.model.ts` e `auth.service.ts` sincronizados com o backend.
5. **Componente de Cadastro no Angular**: Criado `CadastroComponent` (`/cadastro`) integrado ao `POST /api/auth/register`.
6. **Botão de Login/Cadastro com Google**: Adicionado no `LoginComponent` e `CadastroComponent` redirecionando para `/oauth2/authorization/google`.

---

## 📋 Checklist de Verificação

- [x] `./mvnw test` → **46/46 passando** ✅
- [x] `npm test` → **33/33 testes unitários do Angular passando** ✅
- [x] `npm run build` → Angular builda com sucesso em `src/main/resources/static` ✅
- [ ] Acessar `http://localhost:8080` → Angular carrega
- [ ] Login com email+senha → validar na UI
- [ ] Login com Google → validar na UI
- [ ] Cadastro por email+senha → validar na UI
- [ ] Cadastro via Google → validar na UI
- [x] Criptografia de email (AES-256 GCM) funcional no backend ✅

---

> ⚠️ **Nota:** O `.env` foi criado **exclusivamente para fins de desenvolvimento**. Ele foi adicionado ao `.gitignore` e NUNCA deve ser commitado.


## 📂 Estrutura do Projeto

```
BibliotecaPedroMJ/
├── frontend/
│   ├── src/app/
│   │   ├── models/           → AuthModel, LivroModel, UsuarioModel
│   │   ├── services/         → api.service.ts, auth.service.ts
│   │   ├── interceptors/     → auth.interceptor.ts
│   │   ├── guards/           → auth.guard.ts
│   │   ├── pages/            → home, livros, login, dashboard, not-found
│   │   ├── app.config.ts
│   │   └── app.routes.ts
│   ├── angular.json
│   ├── package.json
│   ├── package-lock.json
│   └── src/main/resources/static/  ← output do Angular (build)
│
├── src/main/java/com/pedroMartinsMJ/bibliotecaPedroMJ/
│   ├── controllers/          → LivroController, UsuarioController, AuthController, SpaFallbackController
│   ├── entities/             → Usuario, Livro, BibliotecaPessoal, Role
│   ├── repositorys/          → UsuarioRepository, LivroRepository, RoleRepository
│   ├── services/             → UsuarioService, LivroService, MinioService
│   ├── segurityConfig/       → SecurityConfig, SecurityConfigTest, JWT/, UserDetails/
│   ├── tratamentoDeErros/    → GlobalExceptionHandler, exceptions/
│   └── validators/           → CPFValidator, ValidCPF, ValidacaoUsuario
│
├── src/main/resources/
│   ├── application.yaml
│   ├── application-test.yml
│   └── static/               ← build do Angular
│
├── doc/                       ← 📁 DIREITÓRIO DESTE ARQUIVO
└── pom.xml
```

---

## 🔑 Como Continuar (Próximos Passos)

### Prioridade 1 — Consertar testes de integração
Os 4 erros são todos no `ApplicationContext` dos testes no perfil `test`. Para resolver:

1. **Verificar** que `CustomUserDetailsService` é detectado como `@Service`:
   - Checar se o pacote `com.pedroMartinsMJ.bibliotecaPedroMJ.segurityConfig.UserDetails` está no escopo de component scanning
   - Se necessário, adicionar `@ComponentScan` no `@SpringBootApplication`

2. **Verificar** que `AuthenticationManager` está disponível no perfil `test`:
   - O método `daoAuthenticationProvider()` e `authenticationManager()` em `SecurityConfigTest`
   - Verificar se `DaoAuthenticationProvider` está compilando corretamente

3. **Rodar:** `./mvnw clean test`



- **Backend:** `src/main/java/com/pedroMartinsMJ/bibliotecaPedroMJ/`
- **Frontend:** `frontend/src/app/`
- **Testes Java:** `src/test/java/com/pedroMartinsMJ/bibliotecaPedroMJ/`

---

# 📋 Estado do Projeto — Memória de Revisão

> **Gerado:** 29/10/2026  
> **Para:** Pedro Martins (desenvolvedor do projeto)  
> **Último commit:** `47d070f` — "migração Thymeleaf → Angular, criptografia de dados sensíveis e .env"  
> **Status dos testes:** Backend `./mvnw test` = **42 testes passando, 4 erros**

---
