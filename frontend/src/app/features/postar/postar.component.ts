import { Component, ElementRef, OnDestroy, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse, HttpEventType, HttpResponse } from '@angular/common/http';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Livro, LivroDTO_CREATE } from '../../models/livro.model';

const LIMITE_ARQUIVO_MB = 50;
const LIMITE_CAPA_MB = 5;
const FORMATOS_DE_LIVRO = ['application/pdf', 'application/epub+zip'];
const FORMATOS_DE_CAPA = ['image/jpeg', 'image/png', 'image/webp'];

@Component({
  selector: 'app-postar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page postar-page">
      <div class="page-head">
        <div>
          <h1 class="page-title">Postar novo livro</h1>
          <p class="page-subtitle">
            Você publica como <strong>{{ usuarioAtual }}</strong>. O nome da obra é o do autor
            original; sua conta fica registrada como quem disponibilizou o arquivo.
          </p>
        </div>
      </div>

      <div *ngIf="postado" class="card-panel sucesso-final">
        <p><strong>{{ postado.titulo }}</strong> está no acervo.</p>
        <div class="acoes">
          <a routerLink="/livros" class="btn btn-primary">Ver no catálogo</a>
          <button type="button" class="btn btn-secundario" (click)="publicarOutro()">Postar outro</button>
        </div>
      </div>

      <form *ngIf="!postado" class="form-postar card-panel" (ngSubmit)="salvar()">
        <fieldset class="bloco">
          <legend>Arquivos</legend>

          <div class="campo campo-largo">
            <label for="arquivo">Arquivo do livro (PDF ou EPUB) <span class="obrigatorio">*</span></label>
            <input id="arquivo" type="file" #arquivoInput
                   accept=".pdf,.epub,application/pdf,application/epub+zip"
                   (change)="escolherArquivo($event)" />
            <span class="ajuda">
              {{ arquivo ? arquivo.name + ' · ' + tamanhoFormatado(arquivo.size)
                         : 'Máx. ' + LIMITE_ARQUIVO_MB + ' MB' }}
            </span>
          </div>

          <div class="campo campo-largo">
            <label for="capa">Capa <span class="opcional">(opcional, JPEG, PNG ou WebP)</span></label>
            <div class="previsao">
              <div class="previsao-capa">
                <img *ngIf="urlDaCapa" [src]="urlDaCapa" alt="Capa escolhida" />
                <span *ngIf="!urlDaCapa" class="previsao-vazia">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20M6.5 3H20v18H6.5A2.5 2.5 0 0 1 4 18.5v-13A2.5 2.5 0 0 1 6.5 3z"/>
                  </svg>
                  Sem imagem
                </span>
              </div>
              <div class="campo">
                <input id="capa" type="file" #capaInput
                       accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
                       (change)="escolherCapa($event)" />
                <span class="ajuda">{{ capa ? capa.name + ' · ' + tamanhoFormatado(capa.size)
                                             : 'Máx. ' + LIMITE_CAPA_MB + ' MB' }}</span>
              </div>
            </div>
          </div>
        </fieldset>

        <fieldset class="bloco">
          <legend>Dados da obra</legend>
          <div class="form-grid">
            <div class="campo campo-largo">
              <label for="titulo">Título <span class="obrigatorio">*</span></label>
              <input id="titulo" type="text" [(ngModel)]="livro.titulo" name="titulo"
                     placeholder="Ex.: Dom Casmurro" required />
            </div>
            <div class="campo">
              <label for="autorObra">Autor da obra <span class="obrigatorio">*</span></label>
              <input id="autorObra" type="text" [(ngModel)]="livro.autorObra" name="autorObra"
                     placeholder="Ex.: Machado de Assis" required />
            </div>
            <div class="campo">
              <label for="idioma">Idioma</label>
              <input id="idioma" type="text" [(ngModel)]="livro.idioma" name="idioma" placeholder="pt-BR" />
            </div>
            <div class="campo">
              <label for="isbn">ISBN</label>
              <input id="isbn" type="text" [(ngModel)]="livro.isbn" name="isbn" placeholder="978-3-16-148410-0" />
            </div>
            <div class="campo">
              <label for="editora">Editora</label>
              <input id="editora" type="text" [(ngModel)]="livro.editora" name="editora" />
            </div>
            <div class="campo">
              <label for="dataPublicacao">Data de publicação da obra</label>
              <input id="dataPublicacao" type="date" [(ngModel)]="livro.dataPublicacao" name="dataPublicacao" />
            </div>
            <div class="campo">
              <label for="numeroPaginas">Número de páginas</label>
              <input id="numeroPaginas" type="number" min="1" [(ngModel)]="livro.numeroPaginas" name="numeroPaginas" />
            </div>
            <div class="campo campo-largo">
              <label for="descricao">Descrição</label>
              <textarea id="descricao" rows="3" [(ngModel)]="livro.descricao" name="descricao"
                        placeholder="Sinopse, contexto da edição, domínio público…"></textarea>
            </div>
          </div>
        </fieldset>

        <fieldset *ngIf="precisaIdentidade" class="bloco identidade">
          <legend>Identidade civil</legend>
          <p class="aviso">
            Para publicar é preciso se dar a conhecer: nome real e CPF ficam cifrados e só
            são revelados se um livro for reclamado por uma autoridade. No catálogo continua
            aparecendo apenas o seu usuário.
          </p>
          <div class="form-grid">
            <div class="campo">
              <label for="nomeReal">Nome real <span class="obrigatorio">*</span></label>
              <input id="nomeReal" type="text" [(ngModel)]="nomeReal" name="nomeReal" autocomplete="name" />
            </div>
            <div class="campo">
              <label for="cpf">CPF <span class="obrigatorio">*</span></label>
              <input id="cpf" type="text" [(ngModel)]="cpf" name="cpf" inputmode="numeric"
                     placeholder="Somente números" autocomplete="off" />
            </div>
          </div>
          <div class="acoes">
            <button type="button" class="btn btn-secundario"
                    [disabled]="confirmando || !identidadeInformada"
                    (click)="confirmarIdentidade()">
              {{ confirmando ? 'Confirmando…' : 'Confirmar identidade' }}
            </button>
          </div>
        </fieldset>

        <div *ngIf="carregando" class="progresso">
          <div class="progresso-barra" [style.width.%]="progresso"></div>
          <span class="progresso-rotulo">{{ progresso }}% enviado</span>
        </div>

        <div class="acoes">
          <button type="submit" class="btn btn-primary" [disabled]="carregando || !podeEnviar">
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
    .bloco { border: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 1rem; }
    .bloco legend {
      font-size: 0.78rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em;
      color: var(--ink-400); padding: 0 0 0.6rem;
    }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.1rem 1.25rem; }
    .campo { display: flex; flex-direction: column; gap: 0.4rem; min-width: 0; }
    .campo-largo { grid-column: 1 / -1; }
    label { font-size: 0.85rem; font-weight: 600; color: var(--ink-700); }
    .obrigatorio { color: var(--danger); }
    .opcional { font-weight: 400; color: var(--ink-400); }
    .ajuda { font-size: 0.75rem; color: var(--ink-400); }
    input, textarea {
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
    input[type="file"] { padding: 0.5rem; font-size: 0.85rem; background: var(--ink-100); }
    textarea { resize: vertical; }
    input::placeholder, textarea::placeholder { color: var(--ink-400); }
    input:focus, textarea:focus {
      outline: none;
      background: #fff;
      border-color: var(--brand-500);
      box-shadow: 0 0 0 3px var(--brand-soft);
    }
    .previsao { display: flex; align-items: flex-end; gap: 1rem; }
    .previsao-capa {
      width: 104px;
      aspect-ratio: 2 / 3;
      border-radius: var(--radius-sm);
      overflow: hidden;
      border: 1px dashed var(--ink-200);
      background: var(--ink-100);
      display: grid;
      place-items: center;
      flex-shrink: 0;
    }
    .previsao-capa img { width: 100%; height: 100%; object-fit: cover; }
    .previsao-vazia {
      display: flex; flex-direction: column; align-items: center; gap: 4px;
      font-size: 0.68rem; color: var(--ink-400); text-align: center; padding: 0.5rem;
    }
    .previsao-vazia svg { width: 24px; height: 24px; }
    .identidade {
      padding: 1.1rem;
      border-radius: var(--radius-sm);
      background: #fff8e6;
      border: 1px solid #f0d9a8;
    }
    .aviso { font-size: 0.82rem; color: var(--ink-700); line-height: 1.5; margin: 0; }
    .progresso { position: relative; height: 18px; border-radius: 4px; background: var(--ink-100); overflow: hidden; }
    .progresso-barra { height: 100%; background: var(--brand-gradient); transition: width 0.2s ease; }
    .progresso-rotulo {
      position: absolute; inset: 0; display: grid; place-items: center;
      font-size: 0.7rem; font-weight: 700; color: var(--ink-700);
    }
    .acoes { display: flex; gap: 0.75rem; }
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
      text-decoration: none;
      display: inline-block;
    }
    .btn-secundario { background: var(--ink-100); color: var(--ink-700); border-color: var(--border); box-shadow: none; }
    .btn:hover:not(:disabled) { filter: brightness(1.06); transform: translateY(-1px); }
    .btn:disabled { opacity: 0.55; cursor: not-allowed; transform: none; }
    .mensagem, .sucesso-final {
      padding: 0.8rem 1rem;
      border-radius: var(--radius-sm);
      font-size: 0.88rem; font-weight: 600;
      background: var(--success-bg); color: var(--success-ink);
    }
    .sucesso-final { display: flex; flex-direction: column; gap: 1rem; padding: 1.5rem; }
    .sucesso-final p { margin: 0; }
    .mensagem svg { width: 17px; height: 17px; flex-shrink: 0; vertical-align: -3px; }
    .mensagem-erro { background: var(--danger-bg); color: var(--danger-ink); }
    @media (max-width: 620px) {
      .form-grid { grid-template-columns: 1fr; }
      .form-postar { padding: 1.25rem; }
      .previsao { flex-direction: column; align-items: stretch; }
    }
  `]
})
export class PostarComponent implements OnDestroy {
  readonly LIMITE_ARQUIVO_MB = LIMITE_ARQUIVO_MB;
  readonly LIMITE_CAPA_MB = LIMITE_CAPA_MB;

  livro: LivroDTO_CREATE = { titulo: '', autorObra: '' };
  arquivo: File | null = null;
  capa: File | null = null;
  urlDaCapa = '';

  carregando = false;
  progresso = 0;
  mensagem = '';
  erro = false;

  precisaIdentidade = false;
  confirmando = false;
  nomeReal = '';
  cpf = '';

  postado: Livro | null = null;

  @ViewChild('arquivoInput') private arquivoInput?: ElementRef<HTMLInputElement>;
  @ViewChild('capaInput') private capaInput?: ElementRef<HTMLInputElement>;

  private api = inject(ApiService);
  private auth = inject(AuthService);

  get usuarioAtual(): string {
    return this.auth.getUsuario()?.username ?? 'sua conta';
  }

  get podeEnviar(): boolean {
    return !!(this.livro.titulo?.trim() && this.livro.autorObra?.trim() && this.arquivo);
  }

  get identidadeInformada(): boolean {
    return !!(this.nomeReal.trim() && this.cpf.replace(/\D/g, ''));
  }

  ngOnDestroy(): void {
    this.liberaUrlDaCapa();
  }

  escolherArquivo(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const arquivo = campo.files?.[0] ?? null;

    if (arquivo && !FORMATOS_DE_LIVRO.includes(arquivo.type)) {
      this.arquivo = null;
      this.avisar('O arquivo precisa ser PDF ou EPUB.', true);
      campo.value = '';
      return;
    }
    if (arquivo && arquivo.size > LIMITE_ARQUIVO_MB * 1024 * 1024) {
      this.arquivo = null;
      this.avisar(`O arquivo passa de ${LIMITE_ARQUIVO_MB} MB.`, true);
      campo.value = '';
      return;
    }
    this.arquivo = arquivo;
  }

  escolherCapa(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const capa = campo.files?.[0] ?? null;
    this.liberaUrlDaCapa();
    this.capa = null;

    if (!capa) return;

    if (!FORMATOS_DE_CAPA.includes(capa.type)) {
      this.avisar('A capa precisa ser JPEG, PNG ou WebP.', true);
      campo.value = '';
      return;
    }
    if (capa.size > LIMITE_CAPA_MB * 1024 * 1024) {
      this.avisar(`A capa passa de ${LIMITE_CAPA_MB} MB.`, true);
      campo.value = '';
      return;
    }

    this.capa = capa;
    this.urlDaCapa = URL.createObjectURL(capa);
  }

  salvar(): void {
    if (!this.arquivo) return;

    this.carregando = true;
    this.progresso = 0;
    this.mensagem = '';

    this.api.postarLivro(this.livro, this.arquivo, this.capa).subscribe({
      next: (evento) => {
        if (evento.type === HttpEventType.UploadProgress) {
          this.progresso = evento.total ? Math.round((evento.loaded / evento.total) * 100) : 0;
        } else if (evento instanceof HttpResponse) {
          this.postado = evento.body;
          this.carregando = false;
          this.precisaIdentidade = false;
        }
      },
      error: (err: unknown) => {
        this.carregando = false;
        this.progresso = 0;
        if (this.faltaIdentidade(err)) {
          this.precisaIdentidade = true;
          this.avisar('Para publicar, confirme seu nome real e CPF.', true);
          return;
        }
        this.avisar(mensagemDoErro(err), true);
      },
    });
  }

  confirmarIdentidade(): void {
    this.confirmando = true;
    this.api.confirmarIdentidade({ nomeReal: this.nomeReal.trim(), cpf: this.cpf.replace(/\D/g, '') })
      .subscribe({
        next: () => {
          this.confirmando = false;
          this.precisaIdentidade = false;
          this.avisar('Identidade confirmada. Pode publicar.', false);
        },
        error: (err: unknown) => {
          this.confirmando = false;
          this.avisar(mensagemDoErro(err), true);
        },
      });
  }

  publicarOutro(): void {
    this.postado = null;
    this.livro = { titulo: '', autorObra: '' };
    this.arquivo = null;
    this.capa = null;
    this.liberaUrlDaCapa();
    this.mensagem = '';
    if (this.arquivoInput) this.arquivoInput.nativeElement.value = '';
    if (this.capaInput) this.capaInput.nativeElement.value = '';
  }

  tamanhoFormatado(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`;
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  }

  private liberaUrlDaCapa(): void {
    if (this.urlDaCapa) URL.revokeObjectURL(this.urlDaCapa);
    this.urlDaCapa = '';
  }

  private avisar(mensagem: string, erro: boolean): void {
    this.mensagem = mensagem;
    this.erro = erro;
  }

  /**
   * O backend responde 403 tanto por conta sem identidade civil quanto por falta
   * de permissão; só o primeiro caso tem o campo 'identidade' no corpo, e é o
   * único que a própria tela resolve.
   */
  private faltaIdentidade(err: unknown): boolean {
    return err instanceof HttpErrorResponse
      && err.status === 403
      && !!err.error?.erros?.identidade;
  }
}

function mensagemDoErro(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return 'Não foi possível falar com o servidor.';
    if (err.status === 401) return 'Sua sessão expirou. Entre novamente para postar.';
    const erros = err.error?.erros as Record<string, string> | undefined;
    if (erros) {
      const primeiro = Object.values(erros)[0];
      if (primeiro) return primeiro;
    }
    if (err.error?.mensagem) return err.error.mensagem as string;
    return err.statusText || `Erro ${err.status} ao postar.`;
  }
  return err instanceof Error ? err.message : 'Erro ao postar livro. Tente novamente.';
}
