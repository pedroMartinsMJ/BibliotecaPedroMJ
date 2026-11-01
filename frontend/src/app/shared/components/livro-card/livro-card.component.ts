import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

export interface LivroCard {
  id: string;
  titulo: string;
  autor: string;
  capaUrl?: string;
  categoria?: string;
  anoPublicacao?: number;
}

@Component({
  selector: 'app-livro-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './livro-card.component.html',
  styleUrl: './livro-card.component.css'
})
export class LivroCardComponent {
  @Input({ required: true }) livro!: LivroCard;

  constructor(private router: Router) {}

  abrirDetalhe(): void {
    this.router.navigate(['/livros', this.livro.id]);
  }
}
