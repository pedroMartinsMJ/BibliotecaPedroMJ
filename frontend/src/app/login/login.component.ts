import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Credenciais } from '../models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="page">
      <h1>Login</h1>
      <form (ngSubmit)="entrar()" class="form" novalidate>
        <div class="campo">
          <label for="email">Email</label>
          <input id="email" type="email" name="email" [(ngModel)]="credenciais.email" required />
        </div>
        <div class="campo">
          <label for="senha">Senha</label>
          <input id="senha" type="password" name="senha" [(ngModel)]="credenciais.senha" required />
        </div>
        <button type="submit" class="btn" [disabled]="carregando">
          {{ carregando ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>

      <div class="separador"><span>ou</span></div>

      <a href="/oauth2/authorization/google" class="btn btn-google">
        <svg class="google-icon" viewBox="0 0 24 24" aria-hidden="true">
          <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
          <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
          <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/>
          <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
        </svg>
        Entrar com Google
      </a>

      <div *ngIf="mensagemErro" class="mensagem erro">{{ mensagemErro }}</div>
      <div *ngIf="mensagemSucesso" class="mensagem sucesso">{{ mensagemSucesso }}</div>

      <p class="link-cadastro">
        Não tem conta? <a routerLink="/cadastro">Cadastre-se</a>
      </p>
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 400px;
        margin: 2rem auto;
        padding: 0 1rem;
        font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif;
      }
      h1 { font-size: 1.5rem; margin-bottom: 1.5rem; }
      .form { display: flex; flex-direction: column; gap: 0.9rem; }
      .campo { display: flex; flex-direction: column; gap: 0.35rem; }
      label { font-size: 0.9rem; color: #333; font-weight: 500; }
      input { padding: 0.5rem; font-size: 1rem; border: 1px solid #ccc; border-radius: 4px; }
      input:focus { outline: none; border-color: #0066cc; }
      .btn {
        display: flex; align-items: center; justify-content: center; gap: 0.5rem;
        padding: 0.55rem 1.1rem; background: #333; color: #fff;
        border: none; border-radius: 4px; font-size: 1rem; cursor: pointer;
        text-decoration: none; margin-top: 0.5rem;
      }
      .btn:hover:not(:disabled) { background: #555; }
      .btn:disabled { background: #ccc; cursor: not-allowed; }
      .btn-google { background: #fff; color: #444; border: 1px solid #ccc; }
      .btn-google:hover { background: #f5f5f5; }
      .google-icon { width: 18px; height: 18px; }
      .separador {
        display: flex; align-items: center; gap: 0.75rem;
        margin: 1rem 0; color: #999; font-size: 0.85rem;
      }
      .separador::before, .separador::after {
        content: ''; flex: 1; height: 1px; background: #ddd;
      }
      .mensagem { margin-top: 0.75rem; font-size: 0.9rem; padding: 0.5rem; border-radius: 4px; }
      .mensagem.erro { color: #b00020; background: #fdecea; }
      .mensagem.sucesso { color: #1e7e34; background: #e8f5e9; }
      .link-cadastro { margin-top: 1.25rem; font-size: 0.9rem; text-align: center; color: #555; }
      .link-cadastro a { color: #0066cc; text-decoration: none; }
      .link-cadastro a:hover { text-decoration: underline; }
    `,
  ],
})
export class LoginComponent {
  credenciais: Credenciais = { email: '', senha: '' };
  carregando = false;
  mensagemErro: string | null = null;
  mensagemSucesso: string | null = null;

  constructor(private authService: AuthService, private router: Router) {}

  entrar(): void {
    this.carregando = true;
    this.mensagemErro = null;
    this.mensagemSucesso = null;

    this.authService.login(this.credenciais).subscribe({
      next: () => {
        this.mensagemSucesso = 'Login realizado com sucesso!';
        this.carregando = false;
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.mensagemErro = 'Email ou senha inválidos.';
        this.carregando = false;
      }
    });
  }
}
