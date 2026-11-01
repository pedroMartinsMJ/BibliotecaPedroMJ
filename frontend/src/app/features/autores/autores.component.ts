import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-autores',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="page-head">
        <div>
          <h1 class="page-title">Autores</h1>
          <p class="page-subtitle">Navegue pelos autores cadastrados na biblioteca</p>
        </div>
      </div>

      <div class="alfa card-panel" aria-hidden="true">
        <span class="alfa-letra" *ngFor="let letra of letras">{{ letra }}</span>
      </div>

      <div class="empty-state">
        <span class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </span>
        <h3>Em breve, a lista completa de autores</h3>
        <p>Enquanto isso, explore o catálogo pela busca ou conheça os títulos mais lidos.</p>
        <a class="btn btn-primary" routerLink="/">Ir para o catálogo</a>
      </div>
    </div>
  `,
  styles: [`
    .alfa {
      display: flex;
      flex-wrap: wrap;
      gap: 0.4rem;
      padding: 1rem 1.25rem;
      margin-bottom: 1.25rem;
    }
    .alfa-letra {
      width: 34px;
      height: 34px;
      display: grid;
      place-items: center;
      font-size: 0.8rem;
      font-weight: 700;
      color: var(--ink-500);
      background: var(--ink-50);
      border: 1px solid var(--border);
      border-radius: 9px;
      transition: all 0.15s ease;
    }
    .alfa-letra:first-child {
      color: #fff;
      background: var(--brand-gradient);
      border-color: transparent;
    }
    .empty-state .btn { margin-top: 0.9rem; }
  `]
})
export class AutoresComponent {
  readonly letras = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');
}
