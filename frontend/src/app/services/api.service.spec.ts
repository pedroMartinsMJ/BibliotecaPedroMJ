import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';
import { Livro } from '../models/livro.model';
import { UsuarioDTO_CREATE } from '../models/usuario.model';

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

  it('deve buscar livros da API', () => {
    const livrosMock: Livro[] = [
      { id: '1', titulo: 'Dom Casmurro', autor: 'Machado de Assis' },
      { id: '2', titulo: 'O Cortiço', autor: 'Aluísio Azevedo' }
    ];

    service.getLivros().subscribe((livros) => {
      expect(livros.length).toBe(2);
      expect(livros[0].titulo).toBe('Dom Casmurro');
    });

    const req = httpMock.expectOne('/api/livros');
    expect(req.request.method).toBe('GET');
    req.flush(livrosMock);
  });

  it('deve criar usuário via POST', () => {
    const dto: UsuarioDTO_CREATE = {
      nome: 'Pedro',
      username: 'pedro',
      email: 'pedro@test.com',
      password: 'senha123'
    };
    const usuarioMock = { id: '1', ...dto, ativo: true, data_cadastro: '2024-01-01', roles: [] };

    service.criarUsuario(dto).subscribe((usuario) => {
      expect(usuario.id).toBe('1');
      expect(usuario.username).toBe('pedro');
    });

    const req = httpMock.expectOne('/api/usuario/create');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(dto);
    req.flush(usuarioMock);
  });
});
