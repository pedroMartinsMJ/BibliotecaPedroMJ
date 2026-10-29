import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Credenciais } from '../models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page">
      <h1>Login</h1>
      <form (ngSubmit)="entrar()" class="form" novalidate>
        <div class="campo">
          <label for="username">Usuário / Email</label>
          <input id="username" type="text" name="username" [(ngModel)]="credenciais.username" required />
        </div>
        <div class="campo">
          <label for="senha">Senha</label>
          <input id="senha" type="password" name="senha" [(ngModel)]="credenciais.senha" required />
        </div>
        <button type="submit" class="btn" [disabled]="carregando">
          {{ carregando ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>
      <div *ngIf="mensagemErro" class="mensagem erro">{{ mensagemErro }}</div>
      <div *ngIf="mensagemSucesso" class="mensagem sucesso">{{ mensagemSucesso }}</div>
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
      .btn { padding: 0.55rem 1.1rem; background: #333; color: #fff; border: none; border-radius: 4px; font-size: 1rem; cursor: pointer; }
      .btn:hover:not(:disabled) { background: #555; }
      .btn:disabled { background: #ccc; cursor: not-allowed; }
      .mensagem { margin-top: 0.75rem; font-size: 0.9rem; padding: 0.5rem; border-radius: 4px; }
      .mensagem.erro { color: #b00020; background: #fdecea; }
      .mensagem.sucesso { color: #1e7e34; background: #e8f5e9; }
    `,
  ],
})
export class LoginComponent {
  credenciais: Credenciais = { username: '', senha: '' };
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
        this.mensagemErro = 'Usuário ou senha inválidos.';
        this.carregando = false;
      }
    });
  }
}
