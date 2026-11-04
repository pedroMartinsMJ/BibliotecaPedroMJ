import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpEventType, HttpResponse } from '@angular/common/http';
import { ApiService } from './api.service';
import { Livro } from '../models/livro.model';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('deve buscar a página de livros da API', () => {
    const pagina = {
      content: [
        { id: '1', titulo: 'Dom Casmurro', autorObra: 'Machado de Assis' },
        { id: '2', titulo: 'O Cortiço', autorObra: 'Aluísio Azevedo' }
      ] as Livro[],
      totalElements: 2,
      totalPages: 1,
      number: 0,
      size: 20
    };

    service.getLivros().subscribe((resposta) => {
      expect(resposta.content.length).toBe(2);
      expect(resposta.content[0].titulo).toBe('Dom Casmurro');
    });

    const req = httpMock.expectOne('/api/livros');
    expect(req.request.method).toBe('GET');
    req.flush(pagina);
  });

  it('deve montar a URL da capa servida pelo backend', () => {
    expect(service.urlDaCapa('abc')).toBe('/api/livros/abc/capa');
  });

  it('deve postar livro como multipart com arquivo, capa e metadados', () => {
    const arquivo = new File(['%PDF-1.4'], 'dom-casmurro.pdf', { type: 'application/pdf' });
    const capa = new File(['bin'], 'capa.png', { type: 'image/png' });
    const resposta: Livro = { titulo: 'Dom Casmurro', autorObra: 'Machado de Assis', temArquivo: true };

    let publicado: Livro | null | undefined;
    service.postarLivro(
      { titulo: 'Dom Casmurro', autorObra: 'Machado de Assis', descricao: 'Clássico', dataPublicacao: '1899-12-31', numeroPaginas: 200 },
      arquivo,
      capa
    ).subscribe((evento) => {
      if (evento instanceof HttpResponse) publicado = evento.body;
    });

    const req = httpMock.expectOne('/api/livros');
    expect(req.request.method).toBe('POST');
    const corpo = req.request.body as FormData;
    expect(corpo).toBeInstanceOf(FormData);
    expect(corpo.get('titulo')).toBe('Dom Casmurro');
    expect(corpo.get('autorObra')).toBe('Machado de Assis');
    expect(corpo.get('dataPublicacao')).toBe('1899-12-31');
    expect(corpo.get('numeroPaginas')).toBe('200');
    expect((corpo.get('arquivo') as File).name).toBe('dom-casmurro.pdf');
    expect((corpo.get('capa') as File).name).toBe('capa.png');
    expect(corpo.get('isbn')).toBeNull();

    req.flush(resposta);
    expect(publicado).toEqual(resposta);
  });

  it('deve emitir o progresso do upload antes da resposta', () => {
    const arquivo = new File(['%PDF-1.4'], 'a.pdf', { type: 'application/pdf' });
    const eventos: number[] = [];

    service.postarLivro({ titulo: 'A', autorObra: 'B' }, arquivo, null).subscribe((evento) => {
      if (evento.type === HttpEventType.UploadProgress) eventos.push(evento.loaded);
    });

    const req = httpMock.expectOne('/api/livros');
    req.event({ type: HttpEventType.UploadProgress, loaded: 40, total: 100 });
    req.flush({ titulo: 'A', autorObra: 'B' });

    expect(eventos).toEqual([40]);
    expect((req.request.body as FormData).get('capa')).toBeNull();
  });

  it('deve confirmar identidade civil no endpoint da conta', () => {
    service.confirmarIdentidade({ nomeReal: 'Pedro Martins', cpf: '52998224725' }).subscribe();

    const req = httpMock.expectOne('/usuario/identidade');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ nomeReal: 'Pedro Martins', cpf: '52998224725' });
    req.flush(null);
  });
});
