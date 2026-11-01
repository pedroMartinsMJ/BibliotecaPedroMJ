import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-minha-biblioteca',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-head">
        <div>
          <h1 class="page-title">Minha biblioteca</h1>
          <p class="page-subtitle">Emprestados, favoritos e histórico de leitura em um só lugar</p>
        </div>
        <a class="btn btn-primary" routerLink="/">Explorar catálogo</a>
      </div>

      <div class="estantes">
        <div class="estante card-panel" *ngFor="let secao of secoes">
          <span class="estante-icon" [innerHTML]="secao.icon"></span>
          <h3>{{ secao.titulo }}</h3>
          <p>{{ secao.descricao }}</p>
          <span class="estante-badge">0 itens</span>
        </div>
      </div>

      <div class="empty-state destaque">
        <span class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20M6.5 3H20v18H6.5A2.5 2.5 0 0 1 4 18.5v-13A2.5 2.5 0 0 1 6.5 3z"/>
          </svg>
        </span>
        <h3>Sua biblioteca está vazia</h3>
        <p>Adicione livros aos favoritos ou pegue um emprestado e eles aparecerão aqui.</p>
      </div>
    </div>
  `,
  styles: [`
    .estantes {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1rem;
      margin-bottom: 1.25rem;
    }
    .estante {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      gap: 0.35rem;
      padding: 1.25rem;
      transition: transform 0.18s ease, box-shadow 0.18s ease;
    }
    .estante:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-md);
    }
    .estante-icon {
      width: 40px;
      height: 40px;
      display: grid;
      place-items: center;
      border-radius: 11px;
      background: var(--brand-soft);
      color: var(--brand-500);
      margin-bottom: 0.35rem;
    }
    .estante-icon svg { width: 21px; height: 21px; }
    .estante h3 {
      margin: 0;
      font-size: 1rem;
      font-weight: 700;
      color: var(--ink-900);
    }
    .estante p {
      margin: 0;
      font-size: 0.84rem;
      color: var(--ink-500);
      line-height: 1.45;
    }
    .estante-badge {
      margin-top: 0.5rem;
      font-size: 0.72rem;
      font-weight: 700;
      color: var(--ink-500);
      background: var(--ink-100);
      padding: 0.25rem 0.7rem;
      border-radius: 999px;
    }
    .empty-state.destaque { padding: 3.5rem 2rem; }
  `]
})
export class MinhaBibliotecaComponent {
  readonly secoes = [
    {
      titulo: 'Emprestados',
      descricao: 'Livros que você retirou e ainda está lendo.',
      icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>'
    },
    {
      titulo: 'Favoritos',
      descricao: 'Títulos marcados com estrela para ler depois.',
      icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l2.7 5.6 6 .8-4.4 4.2 1.1 6L12 16.9 6.6 19.6l1.1-6L3.3 9.4l6-.8z"/></svg>'
    },
    {
      titulo: 'Lidos',
      descricao: 'Histórico de leituras concluídas.',
      icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>'
    }
  ];
}
