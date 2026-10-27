package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.TipoArquivo;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositorys.LivroRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.LivroService;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.MinioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {
    @Mock private LivroRepository livroRepository;
    @Mock private MinioService minioService;
    @InjectMocks private LivroService livroService;
    private Livro livroValido;
    private Usuario autor;

    @BeforeEach
    void setUp() {
        autor = new Usuario();
        autor.setId(UUID.randomUUID());
        autor.setNome("Autor Teste");
        autor.setUsername("autor_teste");

        livroValido = new Livro();
        livroValido.setId(UUID.randomUUID());
        livroValido.setTitulo("Livro Teste");
        livroValido.setAutor(autor);
        livroValido.setIsbn("978-3-16-148410-0");
        livroValido.setEditora("Editora Teste");
        livroValido.setIdioma("pt-BR");
        livroValido.setNumeroPaginas(200);
    }

    @Test
    @DisplayName("Deve aceitar arquivo PDF válido")
    void validarArquivo_PdfValido_NaoDeveLancarExcecao() {
        MultipartFile arquivo = mock(MultipartFile.class);
        when(arquivo.isEmpty()).thenReturn(false);
        when(arquivo.getContentType()).thenReturn("application/pdf");
        when(arquivo.getSize()).thenReturn(1024L * 1024L);

        when(minioService.uploadArquivo(any(MultipartFile.class), anyString())).thenReturn("livros/test.pdf");
        when(livroRepository.save(any(Livro.class))).thenAnswer(i -> i.getArgument(0));

        Livro resultado = livroService.criarLivro(livroValido, arquivo, null);

        assertNotNull(resultado, "O livro criado não deve ser nulo");
        assertEquals("livros/test.pdf", resultado.getArquivoKey(), "O arquivo key deve ser definido");
        assertEquals(TipoArquivo.PDF, resultado.getTipoArquivo(), "O tipo deve ser PDF");
    }

    @Test
    @DisplayName("Deve aceitar arquivo EPUB válido")
    void validarArquivo_EpubValido_NaoDeveLancarExcecao() {
        MultipartFile arquivo = mock(MultipartFile.class);
        when(arquivo.isEmpty()).thenReturn(false);
        when(arquivo.getContentType()).thenReturn("application/epub+zip");
        when(arquivo.getSize()).thenReturn(2 * 1024L * 1024L);

        when(minioService.uploadArquivo(any(MultipartFile.class), anyString())).thenReturn("livros/test.epub");
        when(livroRepository.save(any(Livro.class))).thenAnswer(i -> i.getArgument(0));

        Livro resultado = livroService.criarLivro(livroValido, arquivo, null);

        assertNotNull(resultado, "O livro criado não deve ser nulo");
        assertEquals(TipoArquivo.EPUB, resultado.getTipoArquivo(), "O tipo deve ser EPUB");
    }

    @Test
    @DisplayName("Deve rejeitar arquivo vazio")
    void validarArquivo_ArquivoVazio_DeveLancarExcecao() {
        MultipartFile arquivo = mock(MultipartFile.class);
        when(arquivo.isEmpty()).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> livroService.criarLivro(livroValido, arquivo, null),
                "Deve lançar exceção quando o arquivo está vazio");

        assertTrue(ex.getMessage().contains("Falha ao criar livro"), "A mensagem deve informar falha ao criar livro");
    }

    @Test
    @DisplayName("Deve rejeitar arquivo com tipo não suportado")
    void validarArquivo_TipoNaoSuportado_DeveLancarExcecao() {
        MultipartFile arquivo = mock(MultipartFile.class);
        when(arquivo.isEmpty()).thenReturn(false);
        when(arquivo.getContentType()).thenReturn("image/jpeg");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> livroService.criarLivro(livroValido, arquivo, null),
                "Deve lançar exceção quando o tipo de arquivo não é suportado");

        assertTrue(ex.getMessage().contains("Falha ao criar livro"), "A mensagem deve informar falha ao criar livro");
    }

    @Test
    @DisplayName("Deve rejeitar arquivo maior que 50MB")
    void validarArquivo_ArquivoMuitoGrande_DeveLancarExcecao() {
        MultipartFile arquivo = mock(MultipartFile.class);
        when(arquivo.isEmpty()).thenReturn(false);
        when(arquivo.getContentType()).thenReturn("application/pdf");
        when(arquivo.getSize()).thenReturn(51 * 1024L * 1024L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> livroService.criarLivro(livroValido, arquivo, null),
                "Deve lançar exceção quando o arquivo é maior que 50MB");

        assertTrue(ex.getMessage().contains("Falha ao criar livro"), "A mensagem deve informar falha ao criar livro");
    }

    @Test
    @DisplayName("Deve rejeitar livro sem autor")
    void validarLivro_SemAutor_DeveLancarExcecao() {
        livroValido.setAutor(null);

        MultipartFile arquivo = mock(MultipartFile.class);
        when(arquivo.isEmpty()).thenReturn(false);
        when(arquivo.getContentType()).thenReturn("application/pdf");
        when(arquivo.getSize()).thenReturn(1024L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> livroService.criarLivro(livroValido, arquivo, null),
                "Deve lançar exceção quando o livro não tem autor");

        assertTrue(ex.getMessage().contains("Falha ao criar livro"), "A mensagem deve informar falha ao criar livro");
    }

    @Test
    @DisplayName("Deve rejeitar ISBN duplicado")
    void validarLivro_IsbnDuplicado_DeveLancarExcecao() {
        when(livroRepository.existsByIsbn("978-3-16-148410-0")).thenReturn(true);

        MultipartFile arquivo = mock(MultipartFile.class);
        when(arquivo.isEmpty()).thenReturn(false);
        when(arquivo.getContentType()).thenReturn("application/pdf");
        when(arquivo.getSize()).thenReturn(1024L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> livroService.criarLivro(livroValido, arquivo, null),
                "Deve lançar exceção quando o ISBN já está cadastrado");

        assertTrue(ex.getMessage().contains("Falha ao criar livro"), "A mensagem deve informar falha ao criar livro");
    }

    @Test
    @DisplayName("Deve buscar livro por ID com sucesso")
    void buscarPorId_LivroExistente_DeveRetornarLivro() {
        UUID id = UUID.randomUUID();
        when(livroRepository.findById(id)).thenReturn(Optional.of(livroValido));

        Livro resultado = livroService.buscarPorId(id);

        assertNotNull(resultado, "O livro não deve ser nulo");
        assertEquals("Livro Teste", resultado.getTitulo(), "O título deve corresponder");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar livro inexistente")
    void buscarPorId_LivroNaoEncontrado_DeveLancarExcecao() {
        UUID idInexistente = UUID.randomUUID();
        when(livroRepository.findById(idInexistente)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> livroService.buscarPorId(idInexistente),
                "Deve lançar exceção quando o livro não existe");

        assertTrue(ex.getMessage().contains("Livro não encontrado"), "A mensagem deve informar que o livro não foi encontrado");
    }

    @Test
    @DisplayName("Deve deletar livro com sucesso")
    void deletarLivro_LivroExistente_DeveDeletar() {
        UUID id = UUID.randomUUID();
        when(livroRepository.findById(id)).thenReturn(Optional.of(livroValido));
        livroValido.setArquivoKey("livros/test.pdf");
        livroValido.setCapaKey("capas/test.jpg");

        assertDoesNotThrow(() -> livroService.deletarLivro(id), "Não deve lançar exceção ao deletar livro existente");

        verify(minioService, times(2)).deletarArquivo(anyString());
        verify(livroRepository, times(1)).delete(livroValido);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar livro inexistente")
    void deletarLivro_LivroNaoEncontrado_DeveLancarExcecao() {
        UUID idInexistente = UUID.randomUUID();
        when(livroRepository.findById(idInexistente)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> livroService.deletarLivro(idInexistente),
                "Deve lançar exceção ao tentar deletar livro que não existe");

        assertTrue(ex.getMessage().contains("Livro não encontrado"), "A mensagem deve informar que o livro não existe");
    }
}