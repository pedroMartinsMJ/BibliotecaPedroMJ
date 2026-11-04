import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { importProvidersFrom } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { PostarComponent } from './postar.component';
import { Livro } from '../../models/livro.model';

describe('PostarComponent', () => {
  let fixture: ComponentFixture<PostarComponent>;
  let component: PostarComponent;
  let httpMock: HttpTestingController;

  const pdf = () => new File(['%PDF-1.4'], 'livro.pdf', { type: 'application/pdf' });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PostarComponent, HttpClientTestingModule],
      providers: [importProvidersFrom(RouterTestingModule)]
    }).compileComponents();

    fixture = TestBed.createComponent(PostarComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function escreverDados(dados: Partial<Livro> = {}): void {
    component.livro = { titulo: 'Dom Casmurro', autorObra: 'Machado de Assis', ...dados };
    component.arquivo = pdf();
  }

  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  it('não permite enviar sem arquivo', () => {
    component.livro = { titulo: 'Dom Casmurro', autorObra: 'Machado de Assis' };
    expect(component.podeEnviar).toBeFalse();
    expect(component.arquivo).toBeNull();
  });

  it('recusa arquivo que não é PDF nem EPUB', () => {
    const campo = document.createElement('input');
    const evento = { target: campo } as unknown as Event;
    Object.defineProperty(campo, 'files', { value: [new File(['oi'], 'nota.txt', { type: 'text/plain' })] });

    component.escolherArquivo(evento);

    expect(component.arquivo).toBeNull();
    expect(component.erro).toBeTrue();
  });

  it('deve postar os metadados junto com o arquivo', () => {
    escreverDados({ descricao: 'Domínio público' });

    component.salvar();

    const req = httpMock.expectOne('/api/livros');
    const corpo = req.request.body as FormData;
    expect(corpo.get('titulo')).toBe('Dom Casmurro');
    expect(corpo.get('autorObra')).toBe('Machado de Assis');
    expect(corpo.get('descricao')).toBe('Domínio público');
    expect(corpo.get('publicadoPor')).toBeNull();

    req.flush({ titulo: 'Dom Casmurro', autorObra: 'Machado de Assis' } satisfies Livro);
    expect(component.postado?.titulo).toBe('Dom Casmurro');
    expect(component.carregando).toBeFalse();
  });

  it('deve abrir o painel de identidade quando o backend exige o nome civil', () => {
    escreverDados();

    component.salvar();

    const req = httpMock.expectOne('/api/livros');
    req.flush(
      { status: 403, mensagem: 'Requisição inválida', erros: { identidade: 'Confirme seu nome real e CPF antes de publicar' } },
      { status: 403, statusText: 'Forbidden' }
    );

    expect(component.precisaIdentidade).toBeTrue();
    expect(component.postado).toBeNull();
  });

  it('deve publicar de novo depois de confirmar a identidade', () => {
    escreverDados();
    component.precisaIdentidade = true;
    component.nomeReal = 'Pedro Martins';
    component.cpf = '529.982.247-25';

    component.confirmarIdentidade();
    const identidade = httpMock.expectOne('/usuario/identidade');
    identidade.flush({});
    expect((identidade.request.body as { cpf: string }).cpf).toBe('52998224725');

    component.salvar();
    const post = httpMock.expectOne('/api/livros');
    post.flush({ titulo: 'Dom Casmurro', autorObra: 'Machado de Assis' } satisfies Livro);

    expect(component.postado?.titulo).toBe('Dom Casmurro');
    expect(component.precisaIdentidade).toBeFalse();
  });

});
