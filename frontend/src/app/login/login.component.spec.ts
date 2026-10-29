import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../services/auth.service';
import { of, throwError, Subject } from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let loginSubject: Subject<any>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('AuthService', ['login']);
    loginSubject = new Subject();

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule, RouterTestingModule],
      providers: [{ provide: AuthService, useValue: spy }]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  it('deve chamar authService.login ao submeter formulário válido', () => {
    authService.login.and.returnValue(loginSubject.asObservable());

    component.credenciais = { username: 'pedro', senha: '123' };
    component.entrar();

    expect(authService.login).toHaveBeenCalledWith({ username: 'pedro', senha: '123' });

    loginSubject.next({ token: 'abc', usuario: { id: '1', nome: 'Pedro', username: 'pedro', email: 'pedro@test.com', cpf: '12345678901', ativo: true, roles: [] } });
    loginSubject.complete();

    expect(component.mensagemSucesso).toBe('Login realizado com sucesso!');
  });

  it('deve exibir mensagem de erro quando login falha', () => {
    authService.login.and.returnValue(loginSubject.asObservable());

    component.credenciais = { username: 'pedro', senha: 'errada' };
    component.entrar();

    loginSubject.error(new Error('Erro'));

    expect(component.mensagemErro).toBe('Usuário ou senha inválidos.');
    expect(component.carregando).toBeFalse();
  });

  it('deve definir carregando true durante o login', () => {
    authService.login.and.returnValue(loginSubject.asObservable());

    expect(component.carregando).toBeFalse();
    component.entrar();
    expect(component.carregando).toBeTrue();

    loginSubject.next({ token: 'abc', usuario: { id: '1', nome: 'Pedro', username: 'pedro', email: 'pedro@test.com', cpf: '12345678901', ativo: true, roles: [] } });
    loginSubject.complete();
  });
});
