import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { LivroCardComponent, LivroCard } from '../../shared/components/livro-card/livro-card.component';
import { Livro } from '../../models/livro.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, LivroCardComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  livros: LivroCard[] = [];
  carregando = false;
  erro = '';
  totalPaginas = 0;
  paginaAtual = 0;
  tamanhoPagina = 20;
  totalElementos = 0;
  readonly esqueletos = Array.from({ length: 10 });

  constructor(private apiService: ApiService, private router: Router) {}

  ngOnInit(): void {
    this.carregarLivros();
  }

  carregarLivros(): void {
    this.carregando = true;
    this.erro = '';
    this.apiService.getLivros().subscribe({
      next: (pagina) => {
        this.livros = pagina.content.map((livro) => this.paraCard(livro));
        this.totalPaginas = this.limitar(pagina.totalPages, 500);
        this.paginaAtual = Math.min(pagina.number ?? 0, this.totalPaginas - 1);
        this.totalElementos = this.limitar(pagina.totalElements, 1000000) || this.livros.length;
        this.carregando = false;
      },
      error: (err: any) => {
        this.carregando = false;
        this.livros = [];
        this.totalPaginas = 0;
        this.totalElementos = 0;
        this.erro = typeof err?.error === 'string' && err.error
          ? err.error
          : err?.message || 'Não foi possível carregar os livros.';
      },
    });
  }

  // Proteção contra valores absurdos vindos do backend (evita loops/DOM gigante)
  private limitar(valor: number | undefined, maximo: number): number {
    if (typeof valor !== 'number' || !Number.isFinite(valor) || valor < 0) return 0;
    return Math.min(Math.floor(valor), maximo);
  }

  private paraCard(livro: Livro): LivroCard {
    return {
      id: livro.id!,
      titulo: livro.titulo,
      autor: livro.autorObra ?? livro.publicadoPor?.username ?? '',
      capaUrl: livro.id && livro.temCapa ? this.apiService.urlDaCapa(livro.id) : undefined,
      anoPublicacao: livro.dataPublicacao ? Number(livro.dataPublicacao.slice(0, 4)) : undefined,
    };
  }

  irParaPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas) return;
    // Aqui você chamaria o backend com page=... se quiser paginação real
    this.paginaAtual = pagina;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // Janela de páginas com reticências (representadas por -1)
  get paginasVisiveis(): number[] {
    const total = this.totalPaginas;
    const atual = this.paginaAtual;
    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i);
    }

    const paginas: number[] = [0];
    const inicio = Math.max(1, atual - 1);
    const fim = Math.min(total - 2, atual + 1);

    if (inicio > 1) paginas.push(-1);
    for (let i = inicio; i <= fim; i++) paginas.push(i);
    if (fim < total - 2) paginas.push(-1);
    paginas.push(total - 1);

    return paginas;
  }

  trackByIndex(index: number): number {
    return index;
  }

  trackById(_: number, livro: LivroCard): string {
    return livro.id;
  }
}
