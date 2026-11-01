import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-playlist',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-head">
        <div>
          <h1 class="page-title">Playlist</h1>
          <p class="page-subtitle">Sua lista personalizada de livros para ler ou ouvir</p>
        </div>
        <a class="btn btn-primary" routerLink="/">Adicionar livros</a>
      </div>

      <div class="resumo card-panel">
        <div class="resumo-item">
          <span class="resumo-valor">0</span>
          <span class="resumo-rotulo">livros salvos</span>
        </div>
        <div class="resumo-divisor"></div>
        <div class="resumo-item">
          <span class="resumo-valor">0 h</span>
          <span class="resumo-rotulo">de leitura estimada</span>
        </div>
        <div class="resumo-divisor"></div>
        <div class="resumo-item">
          <span class="resumo-valor">—</span>
          <span class="resumo-rotulo">próximo da lista</span>
        </div>
      </div>

      <div class="empty-state">
        <span class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 18a3 3 0 1 1-6 0 3 3 0 0 1 6 0zm12-2a3 3 0 1 1-6 0 3 3 0 0 1 6 0zM9 18V5l12-2v13"/>
          </svg>
        </span>
        <h3>Sua playlist está vazia</h3>
        <p>Toque em um livro no catálogo e salve na playlist para montar sua fila de leitura.</p>
      </div>
    </div>
  `,
  styles: [`
    .resumo {
      display: flex;
      align-items: center;
      justify-content: space-around;
      gap: 1rem;
      padding: 1.4rem 1.5rem;
      margin-bottom: 1.25rem;
    }
    .resumo-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.15rem;
      text-align: center;
    }
    .resumo-valor {
      font-size: 1.5rem;
      font-weight: 800;
      letter-spacing: -0.02em;
      background: var(--brand-gradient);
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
    }
    .resumo-rotulo {
      font-size: 0.78rem;
      font-weight: 600;
      color: var(--ink-500);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .resumo-divisor {
      width: 1px;
      height: 42px;
      background: var(--border);
    }
    @media (max-width: 560px) {
      .resumo { flex-direction: column; gap: 0.9rem; }
      .resumo-divisor { width: 60%; height: 1px; }
    }
  `]
})
export class PlaylistComponent {}
