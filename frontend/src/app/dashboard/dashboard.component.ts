import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="page">
      <h1>Dashboard</h1>
      <p>Área autenticada da biblioteca.</p>
      <div class="actions">
        <a routerLink="/livros" class="btn">Ir para o catálogo</a>
        <a routerLink="/home" class="btn-link">Voltar ao início</a>
      </div>
    </div>
  `,
  styles: [
    `
      .page {
        max-width: 700px;
        margin: 2rem auto;
        padding: 0 1rem;
        font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif;
      }
      h1 { font-size: 1.5rem; margin-bottom: 0.5rem; }
      p { color: #555; margin-bottom: 1.5rem; }
      .actions { display: flex; gap: 0.75rem; }
      .btn {
        display: inline-block;
        padding: 0.55rem 1.1rem;
        border: 1px solid #333;
        text-decoration: none;
        color: #333;
        border-radius: 4px;
        font-size: 0.95rem;
      }
      .btn:hover { background: #f5f5f5; }
      .btn-link { border: none; background: transparent; padding: 0; color: #0066cc; text-decoration: none; cursor: pointer; }
      .btn-link:hover { text-decoration: underline; }
    `,
  ],
})
export class DashboardComponent {}
