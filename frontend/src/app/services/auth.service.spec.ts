import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { Credenciais, AuthResponse } from '../models/auth.model';
import { Usuario } from '../models/usuario.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const credenciaisMock: Credenciais = { email: 'pedro@test.com', senha: 'senha123' };
  const authResponseMock: AuthResponse = {
    token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIn0.fake',
    username: 'pedro',
    nome: 'Pedro',
    email: 'pedro@test.com',
    roles: ['ROLE_USER']
  };
  const usuarioMock: Usuario = {
    username: 'pedro',
    nome: 'Pedro',
    email: 'pedro@test.com',
    roles: ['ROLE_USER']
  };

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
      expect(res.token).toBe(authResponseMock.token);
      expect(res.username).toBe('pedro');
      expect(service.isAuthenticated()).toBe(true);
      expect(service.getToken()).toBe(authResponseMock.token);
      expect(service.getUsuario()?.username).toBe('pedro');
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(credenciaisMock);
    req.flush(authResponseMock);

    expect(localStorage.getItem('biblioteca_token')).toBe(authResponseMock.token);
    expect(localStorage.getItem('biblioteca_usuario')).toBe(JSON.stringify(usuarioMock));
  });

  it('deve cadastrar usuário via register', () => {
    service.register({ nome: 'Pedro', email: 'pedro@test.com', senha: '123' }).subscribe();

    const req = httpMock.expectOne('/api/auth/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ nome: 'Pedro', email: 'pedro@test.com', senha: '123' });
    req.flush(null);
  });

  it('deve limpar sessão ao fazer logout', () => {
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
