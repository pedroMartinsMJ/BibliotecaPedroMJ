import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="page">
      <h1>Página não encontrada</h1>
      <p>A rota que você acessou não existe.</p>
      <a routerLink="/home" class="btn">Voltar ao início</a>
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
    `,
  ],
})
export class NotFoundComponent {}
