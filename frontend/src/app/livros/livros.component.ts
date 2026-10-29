import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-livros',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <h1>Catálogo de Livros</h1>
      <a routerLink="/home" class="btn-link">Voltar ao início</a>

      <div *ngIf="carregando" class="aviso">Carregando livros...</div>
      <div *ngIf="erro" class="erro">Erro ao carregar livros: {{ erro }}</div>

      <ul *ngIf="livros && livros.length" class="lista">
        <li *ngFor="let livro of livros" class="item">
          <strong>{{ livro.titulo || livro }}</strong>
          <span class="meta"> - {{ livro.autor || 'Autor desconhecido' }}</span>
        </li>
      </ul>
      <div *ngIf="livros && livros.length === 0 && !carregando && !erro">
        Nenhum livro encontrado.
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
      h1 { font-size: 1.5rem; margin-bottom: 0.75rem; }
      .btn-link { text-decoration: none; color: #0066cc; margin-bottom: 1rem; display: inline-block; }
      .btn-link:hover { text-decoration: underline; }
      .aviso { color: #666; font-style: italic; margin-bottom: 1rem; }
      .erro { color: #b00020; background: #fdecea; padding: 0.6rem; border-radius: 4px; margin-bottom: 1rem; }
      .lista { list-style: none; padding: 0; margin: 0; }
      .item { padding: 0.6rem 0; border-bottom: 1px solid #eee; }
      .meta { color: #666; }
    `,
  ],
})
export class LivrosComponent implements OnInit {
  livros: any[] = [];
  carregando = true;
  erro: string | null = null;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.getLivros().subscribe({
      next: (data: any) => {
        this.livros = Array.isArray(data) ? data : [];
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Não foi possível carregar os livros.';
        this.carregando = false;
      },
    });
  }
}
