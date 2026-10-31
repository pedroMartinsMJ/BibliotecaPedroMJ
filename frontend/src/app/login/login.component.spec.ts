import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../services/auth.service';
import { Subject } from 'rxjs';

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

    component.credenciais = { email: 'pedro@test.com', senha: '123' };
    component.entrar();

    expect(authService.login).toHaveBeenCalledWith({ email: 'pedro@test.com', senha: '123' });

    loginSubject.next({ token: 'abc', username: 'pedro', nome: 'Pedro', email: 'pedro@test.com', roles: [] });
    loginSubject.complete();

    expect(component.mensagemSucesso).toBe('Login realizado com sucesso!');
  });

  it('deve exibir mensagem de erro quando login falha', () => {
    authService.login.and.returnValue(loginSubject.asObservable());

    component.credenciais = { email: 'pedro@test.com', senha: 'errada' };
    component.entrar();

    loginSubject.error(new Error('Erro'));

    expect(component.mensagemErro).toBe('Email ou senha inválidos.');
    expect(component.carregando).toBeFalse();
  });

  it('deve definir carregando true durante o login', () => {
    authService.login.and.returnValue(loginSubject.asObservable());

    expect(component.carregando).toBeFalse();
    component.entrar();
    expect(component.carregando).toBeTrue();

    loginSubject.next({ token: 'abc', username: 'pedro', nome: 'Pedro', email: 'pedro@test.com', roles: [] });
    loginSubject.complete();
  });
});
