import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-postar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page postar-page">
      <div class="page-head">
        <div>
          <h1 class="page-title">Postar novo livro</h1>
          <p class="page-subtitle">Preencha os dados abaixo para adicionar um título ao acervo</p>
        </div>
      </div>

      <form class="form-postar card-panel" (ngSubmit)="salvar()">
        <div class="form-grid">
          <div class="campo campo-largo">
            <label for="titulo">Título <span class="obrigatorio">*</span></label>
            <input id="titulo" type="text" [(ngModel)]="livro.titulo" name="titulo"
                   placeholder="Ex.: Dom Casmurro" required />
          </div>
          <div class="campo">
            <label for="autor">Autor <span class="obrigatorio">*</span></label>
            <input id="autor" type="text" [(ngModel)]="livro.autor" name="autor"
                   placeholder="Ex.: Machado de Assis" required />
          </div>
          <div class="campo">
            <label for="categoria">Categoria</label>
            <select id="categoria" [(ngModel)]="livro.categoria" name="categoria">
              <option value="">Escolha uma categoria</option>
              <option value="romance">Romance</option>
              <option value="ficcao">Ficção</option>
              <option value="terror">Terror</option>
              <option value="poesia">Poesia</option>
              <option value="historia">História</option>
              <option value="tecnologia">Tecnologia</option>
            </select>
          </div>
          <div class="campo campo-largo">
            <label for="capa">URL da capa <span class="opcional">(opcional)</span></label>
            <input id="capa" type="url" [(ngModel)]="livro.capaUrl" name="capaUrl"
                   placeholder="https://exemplo.com/capa.jpg" />
          </div>
        </div>

        <div class="previsao">
          <span class="previsao-rotulo">Pré-visualização da capa</span>
          <div class="previsao-capa">
            <img *ngIf="livro.capaUrl" [src]="livro.capaUrl" alt="Capa informada" />
            <span *ngIf="!livro.capaUrl" class="previsao-vazia">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20M6.5 3H20v18H6.5A2.5 2.5 0 0 1 4 18.5v-13A2.5 2.5 0 0 1 6.5 3z"/>
              </svg>
              Sem imagem
            </span>
          </div>
        </div>

        <div class="acoes">
          <button type="submit" class="btn btn-primary" [disabled]="carregando || !livro.titulo || !livro.autor">
            {{ carregando ? 'Postando…' : 'Postar livro' }}
          </button>
        </div>

        <div *ngIf="mensagem" class="mensagem" [class.mensagem-erro]="erro">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path *ngIf="!erro" d="M20 6 9 17l-5-5"/>
            <path *ngIf="erro" d="M12 8v5M12 17h.01M12 3l9 17H3z"/>
          </svg>
          {{ mensagem }}
        </div>
      </form>
    </div>
  `,
  styles: [`
    .postar-page { max-width: 760px; }
    .form-postar { padding: 1.75rem; display: flex; flex-direction: column; gap: 1.4rem; }
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.1rem 1.25rem;
    }
    .campo { display: flex; flex-direction: column; gap: 0.4rem; min-width: 0; }
    .campo-largo { grid-column: 1 / -1; }
    label { font-size: 0.85rem; font-weight: 600; color: var(--ink-700); }
    .obrigatorio { color: var(--danger); }
    .opcional { font-weight: 400; color: var(--ink-400); }
    input, select {
      width: 100%;
      padding: 0.7rem 0.9rem;
      font: inherit;
      font-size: 0.92rem;
      color: var(--ink-900);
      background: var(--ink-50);
      border: 1px solid var(--border);
      border-radius: var(--radius-sm);
      transition: all 0.18s ease;
    }
    input::placeholder { color: var(--ink-400); }
    input:focus, select:focus {
      outline: none;
      background: #fff;
      border-color: var(--brand-500);
      box-shadow: 0 0 0 3px var(--brand-soft);
    }
    .previsao { display: flex; align-items: flex-end; gap: 1rem; }
    .previsao-rotulo { font-size: 0.85rem; font-weight: 600; color: var(--ink-700); }
    .previsao-capa {
      width: 104px;
      aspect-ratio: 2 / 3;
      border-radius: var(--radius-sm);
      overflow: hidden;
      border: 1px dashed var(--ink-200);
      background: var(--ink-100);
      display: grid;
      place-items: center;
    }
    .previsao-capa img { width: 100%; height: 100%; object-fit: cover; }
    .previsao-vazia {
      display: flex; flex-direction: column; align-items: center; gap: 4px;
      font-size: 0.68rem; color: var(--ink-400); text-align: center; padding: 0.5rem;
    }
    .previsao-vazia svg { width: 24px; height: 24px; }
    .acoes { display: flex; }
    .btn {
      padding: 0.75rem 1.6rem;
      border: 1px solid transparent;
      font-family: inherit;
      background: var(--brand-gradient);
      color: #fff;
      border-radius: var(--radius-sm);
      font-weight: 700;
      cursor: pointer;
      box-shadow: 0 4px 14px rgba(79,110,247,0.32);
      transition: all 0.18s ease;
    }
    .btn:hover:not(:disabled) { filter: brightness(1.06); transform: translateY(-1px); }
    .btn:disabled { opacity: 0.55; cursor: not-allowed; transform: none; }
    .mensagem {
      display: flex; align-items: center; gap: 0.55rem;
      padding: 0.8rem 1rem;
      border-radius: var(--radius-sm);
      font-size: 0.88rem; font-weight: 600;
      background: var(--success-bg); color: var(--success-ink);
    }
    .mensagem svg { width: 17px; height: 17px; flex-shrink: 0; }
    .mensagem-erro { background: var(--danger-bg); color: var(--danger-ink); }
    @media (max-width: 620px) {
      .form-grid { grid-template-columns: 1fr; }
      .form-postar { padding: 1.25rem; }
    }
  `]
})
export class PostarComponent {
  livro: any = { titulo: '', autor: '', categoria: '', capaUrl: '' };
  carregando = false;
  mensagem = '';
  erro = false;

  constructor(private apiService: ApiService) {}

  salvar(): void {
    this.carregando = true;
    this.mensagem = '';
    this.apiService.criarUsuario(this.livro).subscribe({
      next: () => {
        this.mensagem = 'Livro postado com sucesso!';
        this.erro = false;
        this.carregando = false;
      },
      error: () => {
        this.mensagem = 'Erro ao postar livro. Tente novamente.';
        this.erro = true;
        this.carregando = false;
      },
    });
  }
}
