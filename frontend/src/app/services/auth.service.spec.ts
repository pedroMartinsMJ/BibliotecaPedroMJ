import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { Credenciais } from '../models/auth.model';
import { Usuario } from '../models/usuario.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const credenciaisMock: Credenciais = { username: 'pedro', senha: 'senha123' };
  const usuarioMock: Usuario = {
    id: '1',
    nome: 'Pedro',
    username: 'pedro',
    email: 'pedro@test.com',
    cpf: '12345678901',
    ativo: true,
    roles: ['ROLE_USER']
  };
  const tokenMock = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIn0.fake';
  const authResponseMock = { token: tokenMock, usuario: usuarioMock };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('deve iniciar não autenticado e sem usuário', () => {
    expect(service.isAuthenticated()).toBe(false);
    expect(service.getToken()).toBeNull();
    expect(service.getUsuario()).toBeNull();
  });

  it('deve fazer login, salvar token e usuário', () => {
    service.login(credenciaisMock).subscribe((res) => {
      expect(res.token).toBe(tokenMock);
      expect(res.usuario.username).toBe('pedro');
      expect(service.isAuthenticated()).toBe(true);
      expect(service.getToken()).toBe(tokenMock);
      expect(service.getUsuario()?.username).toBe('pedro');
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(credenciaisMock);
    req.flush(authResponseMock);

    expect(localStorage.getItem('biblioteca_token')).toBe(tokenMock);
    expect(localStorage.getItem('biblioteca_usuario')).toBe(JSON.stringify(usuarioMock));
  });

  it('deve limpar sessão ao fazer logout', () => {
    // Primeiro faz login para criar sessão
    service.login(credenciaisMock).subscribe();
    const reqLogin = httpMock.expectOne('/api/auth/login');
    reqLogin.flush(authResponseMock);

    expect(service.isAuthenticated()).toBe(true);

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.getToken()).toBeNull();
    expect(service.getUsuario()).toBeNull();
    expect(localStorage.getItem('biblioteca_token')).toBeNull();
    expect(localStorage.getItem('biblioteca_usuario')).toBeNull();
  });

  it('deve retornar false para token expirado', () => {
    const tokenExpirado = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjxfY2FrZQ';
    localStorage.setItem('biblioteca_token', tokenExpirado);

    expect(service.isAuthenticated()).toBe(false);
  });
});
