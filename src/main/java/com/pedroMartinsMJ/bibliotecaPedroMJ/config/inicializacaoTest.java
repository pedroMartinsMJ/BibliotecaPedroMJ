package com.pedroMartinsMJ.bibliotecaPedroMJ.config;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Role;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys.RoleRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("test")
public class inicializacaoTest implements CommandLineRunner {

    private final UsuarioService usuarioService;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {

        Role role = new Role();
        role.setDescricao("dsdafsfs");
        role.setName("ROLE_normal");

        Role role1 = new Role();
        role1.setDescricao("normal");
        role1.setName("ROLE_USER");

        roleRepository.save(role);
        roleRepository.save(role1);

        Usuario usuarioTest = new Usuario();
        usuarioTest.setUsername("lucas22");
        usuarioTest.setNome("lucas martins");
        usuarioTest.setPassword("123");
        usuarioTest.setEmail("lucas2020@gmail.com");
        usuarioTest.setCpf("123456789");
        usuarioTest.setTelefone("31 95154485");

        role.setDescricao("teste");
        role.setName("ROLE_normal");
        role.setUsuarios(usuarioTest);

        Livro livro = new Livro();
        livro.setAutor(usuarioTest);
        livro.setTitulo("livro teste");
        livro.setIsbn("h21312312dce");
        livro.setEditora("bilada");
        livro.setDataPublicacao(LocalDate.now());
        livro.setNumeroPaginas(200);
        livro.setIdioma("pt-BR");

        usuarioTest.addRole(role);
        usuarioTest.addLivro(livro);

        usuarioService.salvarUsuario(usuarioTest);
    }
}
