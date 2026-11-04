package com.pedroMartinsMJ.bibliotecaPedroMJ.config;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Identidade;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Role;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.LivroRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.RoleRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Perfil de teste: sobe roles e um autor com uma publicação, na mesma ordem que
 * a vida real respeita (conta -> identidade -> livro), porque cada linha precisa
 * da anterior já existindo no banco.
 */
@Component
@RequiredArgsConstructor
@Profile("test")
public class inicializacaoTest implements CommandLineRunner {

    private final UsuarioService usuarioService;
    private final LivroRepository livroRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        Role roleAdmin = roleRepository.save(new Role("ROLE_ADMIN"));
        roleAdmin.setDescricao("role para administradores do site, nela vc pode mudar estruturas da pagina, como escolher livros que estão nos destaques, ou denunciar uma conta");

        Role roleUser = roleRepository.save(new Role("ROLE_USER"));
        roleUser.setDescricao("role para usuarios normais");

        Usuario usuarioTest = new Usuario("lucas22", "123", "lucas2020@gmail.com");
        usuarioTest.addRole(roleUser);
        usuarioTest = usuarioService.salvarUsuario(usuarioTest);

        Identidade identidade = usuarioService.confirmarIdentidade(
                usuarioTest.getId(), "lucas martins", "52998224725", "seed-de-teste");

        Livro livro = new Livro();
        livro.setTitulo("livro teste");
        livro.setAutorObra("Autor de Teste");
        livro.setIsbn("h21312312dce");
        livro.setEditora("bilada");
        livro.setDataPublicacao(LocalDate.now());
        livro.setNumeroPaginas(200);
        livro.setIdioma("pt-BR");
        livro.setIdentidadePublicador(identidade);
        usuarioTest.addLivro(livro);

        livroRepository.save(livro);
    }
}
