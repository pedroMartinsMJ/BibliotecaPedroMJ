# 📄 Migração Spring Boot 3.x → 4.x

**Projeto:** Biblioteca Pedro MJ  
**Branch:** `fix/ajustes-backend`  
**Data:** 27/10/2026  
**Versão alvo:** Spring Boot 4.1.2 (LTS) + Java 21 (LTS)

---

## 🎯 Por que atualizar?

O Spring Boot 4.x é a versão LTS (Long Term Support) atual. Para um desenvolvedor humano, versões LTS trazem:
- Menos quebras de compatibilidade entre patches
- Bibliotecas e dependências atualizadas automaticamente
- Menos código boilerplate para escrever e revisar
- Suporte de segurança por mais tempo

---

## 🔄 Principais mudanças no `pom.xml`

| Item | Antes (3.x/4.0.x) | Depois (4.1.2) |
|------|-------------------|----------------|
| Spring Boot Parent | `4.0.1` | `4.1.2` |
| Java | `17` | `21` (LTS) |
| `spring-boot-starter-security-oauth2-client` | versão explícita `4.0.1` | versão explícita `4.1.2` |
| `spring-boot-starter-webmvc` | versão explícita `4.0.1` | versão explícita `4.1.2` |
| Testes (security, thymeleaf, validation, webmvc) | versão explícita `4.0.1` | versão explícita `4.1.2` |

---

## 🔐 Mudanças no Spring Security (3.x → 4.x)

### Contexto
O `SecurityConfig.java` e `SecurityConfigTest.java` foram ajustados para funcionar com o Spring Boot 4.1.2.

### O que mudou (resumo para humanos)

| Área | Spring Security 3.x / 5.x | Spring Security 4.x (com Spring Boot 4.x) |
|------|---------------------------|-------------------------------------------|
| Chaves JWT | Injetadas via `@Value("${jwt.public.key}")` | Geradas programaticamente em tempo de execução (desenvolvimento) |
| `PasswordEncoder` | `BCryptPasswordEncoder` como `@Bean` | Mantido, mas verifique se o algoritmo ainda é recomendado |
| `SecurityFilterChain` | Configuração via lambda `.csrf(...)` | Mesmo padrão, mas verifique deprecations na 4.x |
| CORS | `CorsConfigurationSource` como `@Bean` | Mesmo padrão |
| OAuth2 Resource Server | `.oauth2ResourceServer(oauth2 -> oauth2.jwt(...))` | Mesmo padrão |

### ⚠️ Atenção: Chaves JWT em produção

No `SecurityConfig.java` as chaves RSA são geradas aleatoriamente a cada inicialização:

```java
public SecurityConfig() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair keyPair = keyGen.generateKeyPair();
    this.publicKey = (RSAPublicKey) keyPair.getPublic();
    this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
}
```

**Isso significa que tokens JWT emitidos em uma execução não serão válidos na próxima reinicialização.**  
Em produção, gere chaves RSA estáveis e configure via variáveis de ambiente ou arquivo `.pem`.

---

## 🗑️ Entidades e Repositórios Removidos

Foram removidos por não serem usados em nenhum Controller/Service:

| Arquivo | Motivo |
|---------|--------|
| `entities/LivroArquivo.java` | Entidade órfã, sem referência |
| `entities/Profile.java` | Entidade órfã, sem referência |
| `repositorys/CompraRepository.java` | Duplicava `BibliotecaPessoalRepository` |

---

## 🐛 Bugs Corrigidos

| # | Arquivo | Problema | Correção |
|---|---------|----------|----------|
| 1 | `UsuarioService.java` | `atualizarDados` copiava valores do banco para si mesmo (`usuario.setNome(usuario.getNome())`) | Agora usa `usuarioAtualizado.getNome()` etc. |
| 2 | `LivroDTO_RESPONSE.java` | `AutorDTO` expunha `email` do autor (dado pessoal) | Campo `email` removido do `AutorDTO` |
| 3 | `UsuarioController.java` | `GET /usuario/tudo` sem restrição de role | Adicionado `@PreAuthorize("hasRole('ROLE_ADMIN')")` |
| 4 | `LivroDTO_CREATE.java` | `capa` marcada como `@NotNull` obrigatória | Removida anotação (capa é opcional no service) |
| 5 | `LivroService.java` | `@PostConstruct init()` tentava conectar ao MinIO nos testes | Método `init()` removido (bucket é criado via Docker Compose) |
| 6 | `pom.xml` | Java 25 / Spring Boot 4.0.1 | Java 21 LTS / Spring Boot 4.1.2 LTS |

---

## 📦 Paginação Adicionada

| Endpoint | Antes | Depois |
|----------|-------|--------|
| `GET /api/livros` | `List<Livro>` (sem paginação, N+1) | `Page<LivroDTO_RESPONSE>` com `Pageable` (20 itens/página) |
| `GET /api/livros/autor/{id}` | `List<Livro>` | `Page<LivroDTO_RESPONSE>` com `Pageable` |
| Repository | `findAll()` | `findAllWithAutor(Pageable)` com `JOIN FETCH` (evita N+1) |

---

## 🔑 Variáveis de Ambiente

Credenciais sensíveis movidas do `application.yaml` para variáveis de ambiente (com valores default para desenvolvimento):

```yaml
datasource:
  username: ${DB_USERNAME:pedro}
  password: ${DB_PASSWORD:senha123}

jwt:
  private:
    key: ${JWT_PRIVATE_KEY:classpath:app.key}
  public:
    key: ${JWT_PUBLIC_KEY:classpath:app.pub}

minio:
  endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin123}
```

---

## ✅ Testes

| Classe | Testes | Status |
|--------|--------|--------|
| `CPFValidatorTest` | 11 | ✅ Passando |
| `LivroControllerTest` | 3 | ✅ Passando |
| `BibliotecaPedroMjApplicationTests` | 1 | ✅ Passando |
| **Total** | **15** | **✅ BUILD SUCCESS** |

---

## 📚 Próximos passos sugeridos

1. Gerar chaves RSA estáveis para produção (substituir a geração aleatória)
2. Implementar endpoints da Biblioteca Pessoal do usuário
3. Adicionar Swagger/OpenAPI (`springdoc-openapi`)
4. Adicionar mais testes de unidade para `UsuarioService` e `LivroService`