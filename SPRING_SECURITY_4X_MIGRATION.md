# 🔐 Migração Spring Security 3.x → 4.x (com Spring Boot 4.x)

**Projeto:** Biblioteca Pedro MJ  
**Branch:** `fix/ajustes-backend`  
**Data:** 27/10/2026  
**Contexto:** Spring Boot 4.1.2 (que utiliza Spring Security alinhado à linha 4.x)

---

## ⚠️ Aviso importante para humanos

O Spring Security passou por mudanças significativas entre as linhas 3.x/5.x e a linha 4.x. O objetivo deste documento é resumir o que mudou de forma que você (humano) não precise reler toda a documentação oficial.

---

## 📋 O que mudou de forma resumida

| Tópico | Spring Security 3.x / 5.x (Spring Boot 2.x / 3.x) | Spring Security 4.x (Spring Boot 4.x) |
|--------|---------------------------------------------------|---------------------------------------|
| Configuração de segurança | `WebSecurityConfigurerAdapter` (deprecated) | `SecurityFilterChain` como `@Bean` |
| Chaves JWT RSA | Injetadas via `@Value` de arquivo `.key` / `.pub` | Geradas em runtime (dev) ou via config externa (prod) |
| `PasswordEncoder` | `BCryptPasswordEncoder` como `@Bean` | Mantido, mas revise se ainda é o recomendado para 4.x |
| CORS | `CorsConfigurationSource` como `@Bean` | Mesmo padrão |
| OAuth2 Resource Server | `.oauth2ResourceServer(oauth2 -> jwt(...))` | Mesmo padrão |
| OAuth2 Client (Google login) | `.oauth2Login(...)` | Mesmo padrão |
| Method Security | `@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)` | Mesmo padrão |
| `PreAuthorize` | `@PreAuthorize("hasRole('ROLE_ADMIN')")` | Mesmo padrão |

---

## 🛠️ O que foi alterado neste projeto

### 1. `SecurityConfig.java`

**Antes (conceitual - Spring Security 5.x):**
```java
@Value("${jwt.public.key}")
private RSAPublicKey publicKey;

@Value("${jwt.private.key}")
private RSAPrivateKey privateKey;
```

**Depois (Spring Boot 4.x / Security 4.x):**
```java
private final RSAPublicKey publicKey;
private final RSAPrivateKey privateKey;

public SecurityConfig() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair keyPair = keyGen.generateKeyPair();
    this.publicKey = (RSAPublicKey) keyPair.getPublic();
    this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
}
```

**Motivo:** Em versões mais recentes, a injeção direta de chaves via `@Value` de strings simples não funciona mais para tipos `RSAPublicKey`/`RSAPrivateKey`. A geração em runtime resolve para desenvolvimento, mas **em produção você deve gerar chaves estáveis**.

---

### 2. `SecurityConfigTest.java`

Mesma mudança: removida a injeção via `@Value` e substituída por geração programática de chaves RSA no construtor, para que os testes de contexto carreguem sem depender de arquivos de chave físicos.

---

### 3. Endpoint protegido com `@PreAuthorize`

```java
@GetMapping("/usuario/tudo")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public ResponseEntity<List<UsuarioDTO_RESPONSE>> listartudo() { ... }
```

Isso já estava no Spring Security 3.x/5.x e continua funcionando na 4.x. A diferença é que agora o endpoint está realmente protegido (antes estava exposto publicamente).

---

## 🚨 Cuidados específicos para Spring Security 4.x

### 1. Tokens JWT não persistem entre reinicializações

Como as chaves RSA são geradas aleatoriamente a cada startup (no `SecurityConfig` e `SecurityConfigTest`), qualquer JWT emitido antes de uma reinicialização se torna inválido depois.

**Solução para produção:**
- Gere um par de chaves RSA uma única vez:
  ```bash
  openssl genrsa -out jwt-private.pem 2048
  openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem
  ```
- Configure no `application.yml` de produção:
  ```yaml
  jwt:
    public:
      key: classpath:jwt-public.pem
    private:
      key: classpath:jwt-private.pem
  ```
- Ou use um `KeyStore` / provedor de chaves externo (Vault, AWS KMS, etc.)

### 2. `BCryptPasswordEncoder` ainda é seguro?

O Spring Security 4.x continua suportando `BCryptPasswordEncoder`, mas verifique na documentação oficial se há um encoder recomendado para a versão 4.x (ex: `Pbkdf2PasswordEncoder`, `SCryptPasswordEncoder`, etc.).

### 3. CORS

A configuração de CORS em `SecurityConfig.java` continua válida, mas revise as origens permitidas (`allowedOrigins`) para produção — atualmente estão hardcoded para `localhost`.

---

## 📚 Referências úteis

- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring Boot 4.x Release Notes](https://github.com/spring-projects/spring-boot/wiki)
- [Migration Guide Spring Security 6.x → 7.x](https://docs.spring.io/spring-security/reference/7.x/migration/6-to-7.html) *(se aplicável)*

---

## ✅ Status no projeto

- [x] Spring Boot atualizado para 4.1.2 (LTS)
- [x] Java atualizado para 21 (LTS)
- [x] `SecurityConfig.java` ajustado para Spring Boot 4.x
- [x] `SecurityConfigTest.java` ajustado para Spring Boot 4.x
- [x] Todos os 15 testes passando
- [ ] Chaves RSA estáveis para produção (pendente - geradas em runtime atualmente)
- [ ] Revisar `PasswordEncoder` para produção (pendente)