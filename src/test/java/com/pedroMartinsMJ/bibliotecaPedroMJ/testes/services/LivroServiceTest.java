package com.pedroMartinsMJ.bibliotecaPedroMJ.testes.services;

import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Identidade;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Livro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.Usuario;
import com.pedroMartinsMJ.bibliotecaPedroMJ.entities.enums.TipoArquivo;
import com.pedroMartinsMJ.bibliotecaPedroMJ.repositories.LivroRepository;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.EscritaTransacionalDeLivro;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.InspecionadorDeArquivo;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.LivroService;
import com.pedroMartinsMJ.bibliotecaPedroMJ.services.MinioService;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.ArquivoIndisponivelException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.ArquivoInvalidoException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.tratamentoDeErros.exceptions.LivroNaoEncontradoException;
import com.pedroMartinsMJ.bibliotecaPedroMJ.validators.ValidacaoDeLivro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    private static final String KEY_ARQUIVO = "livros/2026-11-01/abc.pdf";
    private static final String KEY_CAPA = "capas/2026-11-01/def.jpg";

    @Mock private LivroRepository livroRepository;
    @Mock private MinioService minioService;
    @Mock private InspecionadorDeArquivo inspecionador;
    @Mock private EscritaTransacionalDeLivro escrita;
    @Mock private ValidacaoDeLivro validacaoDeLivro;
    @InjectMocks private LivroService livroService;

    private Livro livroValido;

    @BeforeEach
    void setUp() {
        Usuario autor = new Usuario();
        autor.setId(UUID.randomUUID());
        autor.setUsername("autor_teste");
        autor.setIdentidadeAtual(new Identidade(autor, "Autor Teste", "52998224725", "autodeclaracao"));

        livroValido = new Livro();
        livroValido.setId(UUID.randomUUID());
        livroValido.setTitulo("Livro Teste");
        livroValido.setAutor(autor);
        livroValido.setIsbn("978-3-16-148410-0");
    }

    private MultipartFile pdf() {
        MultipartFile arquivo = mock(MultipartFile.class);
        when(arquivo.getSize()).thenReturn(1024L * 1024L);
        return arquivo;
    }

    @Test
    @DisplayName("Deve criar livro com PDF válido")
    void criarLivro_PdfValido_DevePersistir() {
        MultipartFile arquivo = pdf();
        when(inspecionador.inspecionarCorpo(arquivo)).thenReturn("application/pdf");
        when(minioService.uploadArquivo(eq(arquivo), eq("livros"), eq("application/pdf"))).thenReturn(KEY_ARQUIVO);
        when(escrita.salvar(any(Livro.class))).thenAnswer(i -> i.getArgument(0));

        Livro resultado = livroService.criarLivro(livroValido, arquivo, null);

        assertNotNull(resultado);
        assertEquals(KEY_ARQUIVO, resultado.getArquivoKey());
        assertEquals(TipoArquivo.PDF, resultado.getTipoArquivo());
        assertNotNull(resultado.getIdentidadePublicador(),
                "O livro nasce com a fotografia da identidade vigente de quem publicou");
        verify(validacaoDeLivro).validarCadastro(livroValido);
    }

    @Test
    @DisplayName("Deve enviar capa com o content type detectado no conteúdo")
    void criarLivro_ComCapa_DeveUsarContentTypeDetectado() {
        MultipartFile arquivo = pdf();
        MultipartFile capa = mock(MultipartFile.class);
        when(capa.getSize()).thenReturn(2048L);
        when(inspecionador.inspecionarCorpo(arquivo)).thenReturn("application/pdf");
        when(inspecionador.inspecionarCapa(capa)).thenReturn("image/png");
        when(minioService.uploadArquivo(eq(arquivo), eq("livros"), eq("application/pdf"))).thenReturn(KEY_ARQUIVO);
        when(minioService.uploadArquivo(eq(capa), eq("capas"), eq("image/png"))).thenReturn(KEY_CAPA);
        when(escrita.salvar(any(Livro.class))).thenAnswer(i -> i.getArgument(0));

        Livro resultado = livroService.criarLivro(livroValido, arquivo, capa);

        assertEquals("image/png", resultado.getCapaContentType(),
                "O content type servido é o do conteúdo, não o declarado pelo navegador");
    }

    @Test
    @DisplayName("Deve remover objetos recém-enviados quando o banco falha")
    void criarLivro_FalhaNoBanco_DeveCompensarMinIO() {
        MultipartFile arquivo = pdf();
        when(inspecionador.inspecionarCorpo(arquivo)).thenReturn("application/pdf");
        when(minioService.uploadArquivo(eq(arquivo), eq("livros"), eq("application/pdf"))).thenReturn(KEY_ARQUIVO);
        when(escrita.salvar(any(Livro.class))).thenThrow(new IllegalStateException("banco caiu"));

        assertThrows(IllegalStateException.class, () -> livroService.criarLivro(livroValido, arquivo, null));

        verify(minioService).deletarArquivo(KEY_ARQUIVO);
    }

    @Test
    @DisplayName("Não deve tocar no storage quando a validação falha antes do upload")
    void criarLivro_ValidacaoFalha_NaoDeveEnviarArquivo() {
        MultipartFile arquivo = mock(MultipartFile.class);
        when(inspecionador.inspecionarCorpo(arquivo)).thenThrow(new ArquivoInvalidoException("arquivo", "Apenas PDF e EPUB são aceitos"));

        assertThrows(ArquivoInvalidoException.class, () -> livroService.criarLivro(livroValido, arquivo, null));

        verify(minioService, never()).uploadArquivo(any(), anyString(), anyString());
        verify(escrita, never()).salvar(any());
    }

    @Test
    @DisplayName("Falha ao compensar não deve esconder o erro original")
    void criarLivro_CompensacaoFalha_DevePropagarErroOriginal() {
        MultipartFile arquivo = pdf();
        when(inspecionador.inspecionarCorpo(arquivo)).thenReturn("application/pdf");
        when(minioService.uploadArquivo(eq(arquivo), eq("livros"), eq("application/pdf"))).thenReturn(KEY_ARQUIVO);
        when(escrita.salvar(any(Livro.class))).thenThrow(new IllegalStateException("banco caiu"));
        doThrow(new RuntimeException("minio fora do ar")).when(minioService).deletarArquivo(KEY_ARQUIVO);

        assertThrows(IllegalStateException.class, () -> livroService.criarLivro(livroValido, arquivo, null));
    }

    @Test
    @DisplayName("Deve buscar livro com autor resolvido")
    void buscarPorId_LivroExistente_DeveRetornarComAutor() {
        when(livroRepository.findComAutorById(livroValido.getId())).thenReturn(Optional.of(livroValido));

        Livro resultado = livroService.buscarPorId(livroValido.getId());

        assertNotNull(resultado.getAutor(), "O autor precisa vir carregado para o mapper");
    }

    @Test
    @DisplayName("Deve lançar exceção específica quando o livro não existe")
    void buscarPorId_Inexistente_DeveLancarExcecaoDeDominio() {
        UUID id = UUID.randomUUID();
        when(livroRepository.findComAutorById(id)).thenReturn(Optional.empty());

        assertThrows(LivroNaoEncontradoException.class, () -> livroService.buscarPorId(id));
    }

    @Test
    @DisplayName("Deve recusar download de livro sem arquivo")
    void downloadArquivo_SemArquivo_DeveLancarExcecao() {
        assertThrows(ArquivoIndisponivelException.class, () -> livroService.downloadArquivo(livroValido));
    }

    @Test
    @DisplayName("Deve recusar download de capa quando o livro não tem capa")
    void downloadCapa_SemCapa_DeveLancarExcecao() {
        assertThrows(ArquivoIndisponivelException.class, () -> livroService.downloadCapa(livroValido));
    }

    @Test
    @DisplayName("Deve escrever a nova chave antes de liberar a antiga ao trocar o arquivo")
    void atualizarArquivo_DeveManterArquivoAntigoAteOBancoConfirmar() {
        MultipartFile arquivo = pdf();
        livroValido.setArquivoKey("livros/antigo.pdf");
        when(livroRepository.findComAutorById(livroValido.getId())).thenReturn(Optional.of(livroValido));
        when(inspecionador.inspecionarCorpo(arquivo)).thenReturn("application/epub+zip");
        when(minioService.uploadArquivo(eq(arquivo), eq("livros"), eq("application/epub+zip"))).thenReturn(KEY_ARQUIVO);
        when(escrita.salvar(any(Livro.class))).thenAnswer(i -> i.getArgument(0));

        Livro resultado = livroService.atualizarArquivo(livroValido.getId(), arquivo);

        assertEquals(TipoArquivo.EPUB, resultado.getTipoArquivo());
        // ordem: gravar metadados → só então apagar o objeto antigo
        InOrder ordem = inOrder(escrita, minioService);
        ordem.verify(escrita).salvar(any(Livro.class));
        ordem.verify(minioService).deletarArquivo("livros/antigo.pdf");
    }

    @Test
    @DisplayName("Falha no banco ao trocar o arquivo deve manter o arquivo antigo intacto")
    void atualizarArquivo_FalhaNoBanco_NaoDevePerderArquivoAntigo() {
        MultipartFile arquivo = pdf();
        livroValido.setArquivoKey("livros/antigo.pdf");
        when(livroRepository.findComAutorById(livroValido.getId())).thenReturn(Optional.of(livroValido));
        when(inspecionador.inspecionarCorpo(arquivo)).thenReturn("application/pdf");
        when(minioService.uploadArquivo(eq(arquivo), eq("livros"), eq("application/pdf"))).thenReturn(KEY_ARQUIVO);
        when(escrita.salvar(any(Livro.class))).thenThrow(new IllegalStateException("banco caiu"));

        assertThrows(IllegalStateException.class, () -> livroService.atualizarArquivo(livroValido.getId(), arquivo));

        verify(minioService).deletarArquivo(KEY_ARQUIVO);
        verify(minioService, never()).deletarArquivo("livros/antigo.pdf");
    }

    @Test
    @DisplayName("Trocar a capa não deve apagar a antiga antes do banco confirmar")
    void atualizarCapa_FalhaNoBanco_DeveManterCapaAntiga() {
        MultipartFile novaCapa = mock(MultipartFile.class);
        when(novaCapa.getSize()).thenReturn(2048L);
        livroValido.setCapaKey("capas/antiga.jpg");
        when(livroRepository.findComAutorById(livroValido.getId())).thenReturn(Optional.of(livroValido));
        when(inspecionador.inspecionarCapa(novaCapa)).thenReturn("image/jpeg");
        when(minioService.uploadArquivo(eq(novaCapa), eq("capas"), eq("image/jpeg"))).thenReturn(KEY_CAPA);
        when(escrita.salvar(any(Livro.class))).thenThrow(new IllegalStateException("banco caiu"));

        assertThrows(IllegalStateException.class, () -> livroService.atualizarCapa(livroValido.getId(), novaCapa));

        verify(minioService).deletarArquivo(KEY_CAPA);
        verify(minioService, never()).deletarArquivo("capas/antiga.jpg");
    }

    @Test
    @DisplayName("Remover capa deve limpar o banco antes de apagar o objeto")
    void removerCapa_DeveLimparBancoPrimeiro() {
        livroValido.setCapaKey(KEY_CAPA);
        when(livroRepository.findComAutorById(livroValido.getId())).thenReturn(Optional.of(livroValido));

        livroService.removerCapa(livroValido.getId());

        InOrder ordem = inOrder(escrita, minioService);
        ordem.verify(escrita).salvar(livroValido);
        ordem.verify(minioService).deletarArquivo(KEY_CAPA);
    }

    @Test
    @DisplayName("Deve deletar livro no banco e no storage")
    void deletarLivro_LivroExistente_DeveRemoverBancoEStrorage() {
        UUID id = livroValido.getId();
        livroValido.setArquivoKey(KEY_ARQUIVO);
        livroValido.setCapaKey(KEY_CAPA);
        when(livroRepository.findComAutorById(id)).thenReturn(Optional.of(livroValido));

        livroService.deletarLivro(id);

        verify(escrita, times(1)).excluir(livroValido);
        verify(minioService, times(2)).deletarArquivo(anyString());
    }

    @Test
    @DisplayName("Livro inexistente deve dar 404 de domínio ao deletar")
    void deletarLivro_Inexistente_DeveLancarExcecaoDeDominio() {
        UUID id = UUID.randomUUID();
        when(livroRepository.findComAutorById(id)).thenReturn(Optional.empty());

        assertThrows(LivroNaoEncontradoException.class, () -> livroService.deletarLivro(id));
    }
}
