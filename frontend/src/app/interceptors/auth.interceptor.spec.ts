import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

describe('AuthInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('AuthService', ['getToken', 'isAuthenticated']);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: spy }
      ]
    });
    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve adicionar header Authorization quando houver token', () => {
    authService.getToken.and.returnValue('token-fake');
    authService.isAuthenticated.and.returnValue(true);

    httpClient.get('/api/teste').subscribe();

    const req = httpMock.expectOne('/api/teste');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-fake');
    req.flush({ ok: true });
  });

  it('não deve adicionar header Authorization quando não houver token', () => {
    authService.getToken.and.returnValue(null);

    httpClient.get('/api/teste').subscribe();

    const req = httpMock.expectOne('/api/teste');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({ ok: true });
  });

  it('não deve anexar token vencido, para a listagem pública continuar 200', () => {
    authService.getToken.and.returnValue('token-vencido');
    authService.isAuthenticated.and.returnValue(false);

    httpClient.get('/api/livros').subscribe();

    const req = httpMock.expectOne('/api/livros');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({ content: [] });
  });
});

