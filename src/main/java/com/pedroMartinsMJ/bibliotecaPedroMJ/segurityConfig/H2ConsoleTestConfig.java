package com.pedroMartinsMJ.bibliotecaPedroMJ.segurityConfig;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Registra o H2 Console como servlet Jakarta.
 *
 * Spring Boot 4.x removeu o H2ConsoleAutoConfiguration, então a propriedade
 * spring.h2.console.enabled não registra mais nada. O console embutido do H2
 * (org.h2.server.web.WebServlet) é javax.* e não roda em Tomcat 11; a classe
 * JakartaWebServlet é a variante compatível.
 */
@Configuration
@Profile("test")
public class H2ConsoleTestConfig {

    @Bean
    public ServletRegistrationBean<JakartaWebServlet> h2ConsoleServletRegistration() {
        ServletRegistrationBean<JakartaWebServlet> registration =
                new ServletRegistrationBean<>(new JakartaWebServlet(), "/h2-console/*");
        registration.setLoadOnStartup(1);
        registration.addInitParameter("allow-other", "false");
        registration.addInitParameter("trace", "false");
        return registration;
    }
}
