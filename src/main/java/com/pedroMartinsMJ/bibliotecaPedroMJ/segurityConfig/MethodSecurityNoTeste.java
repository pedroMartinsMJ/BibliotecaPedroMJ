package com.pedroMartinsMJ.bibliotecaPedroMJ.segurityConfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Liga a checagem de @PreAuthorize/@Secured dentro do profile 'test'.
 *
 * O profile 'test' serve a dois publicos com vontades opostas: a suíte unitária
 * quer o 403 de ADMIN acontecendo de verdade, e o launcher do modo teste quer o
 * dev abrindo qualquer página sem login. Por isso o switch é uma propriedade e
 * não o profile: o launcher define APP_TESTE_METODO_SEGURANCA=false e as anotações
 * deixam de ser aplicadas; quem roda 'mvnw test' não define nada e continua
 * cobrindo a autorização por método.
 */
@Configuration(proxyBeanMethods = false)
@Profile("test")
@ConditionalOnProperty(name = "app.teste.metodo-seguranca", havingValue = "true", matchIfMissing = true)
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class MethodSecurityNoTeste {
}
