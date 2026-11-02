package com.pedroMartinsMJ.bibliotecaPedroMJ.config;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Role;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.RoleRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.UsuarioRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Popula o banco da simulação de produção (biblioteca_prod) com as roles e um
 * usuário de exemplo, para haver dados visíveis no pgAdmin/Adminer.
 *
 * Idempotente: só insere o que ainda não existe, porque ao contrário do profile
 * test o schema aqui persiste entre reinicializações.
 */
@Component
@RequiredArgsConstructor
@Profile("prodsim")
@ConditionalOnProperty(name = "app.seed-demo", havingValue = "true")
public class SeedDadosProducaoSimulada implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Override
    @Transactional
    public void run(String... args) {
        Role roleAdmin = criarRoleSeNaoExiste("ROLE_ADMIN",
                "role para administradores do site");
        criarRoleSeNaoExiste("ROLE_USER", "role para usuarios normais");

        if (!usuarioRepository.existsByUsername("simulacao")) {
            Usuario usuario = new Usuario();
            usuario.setUsername("simulacao");
            usuario.setEmail("simulacao@biblioteca.local");
            usuario.setPassword("senha-de-simulacao");
            usuario.addRole(roleAdmin);
            usuario = usuarioService.salvarUsuario(usuario);

            usuarioService.confirmarIdentidade(usuario.getId(),
                    "Usuario de Simulacao", "52998224725", "seed-de-simulacao");
        }
    }

    private Role criarRoleSeNaoExiste(String nome, String descricao) {
        return roleRepository.findByName(nome).orElseGet(() -> {
            Role role = new Role();
            role.setName(nome);
            role.setDescricao(descricao);
            return roleRepository.save(role);
        });
    }
}
