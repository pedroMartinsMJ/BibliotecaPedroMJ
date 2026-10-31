import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { CadastroComponent } from './cadastro.component';
import { AuthService } from '../services/auth.service';
import { of, throwError } from 'rxjs';

describe('CadastroComponent', () => {
  let component: CadastroComponent;
  let fixture: ComponentFixture<CadastroComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthService', ['register']);

    await TestBed.configureTestingModule({
      imports: [CadastroComponent, ReactiveFormsModule, RouterTestingModule],
      providers: [
        { provide: AuthService, useValue: authSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CadastroComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
  });

  it('deve ser criado', () => {
    expect(component).toBeTruthy();
  });

  it('deve chamar authService.register ao cadastrar e redirecionar', fakeAsync(() => {
    authService.register.and.returnValue(of(undefined as any));

    component.dados = { nome: 'Pedro', email: 'pedro@test.com', senha: '123456' };
    component.cadastrar();

    expect(authService.register).toHaveBeenCalledWith({ nome: 'Pedro', email: 'pedro@test.com', senha: '123456' });
    expect(component.mensagemSucesso).toContain('Conta criada!');

    tick(1500);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  }));

  it('deve exibir mensagem de erro quando o cadastro falha', () => {
    authService.register.and.returnValue(throwError(() => ({ error: { message: 'Email duplicado' } })));

    component.dados = { nome: 'Pedro', email: 'pedro@test.com', senha: '123456' };
    component.cadastrar();

    expect(component.mensagemErro).toBe('Email duplicado');
    expect(component.carregando).toBeFalse();
  });
});
