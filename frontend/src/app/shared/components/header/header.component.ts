import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  @Output() toggleSidebar = new EventEmitter<void>();

  busca = '';
  categoriaSelecionada = '';
  ordenacao = 'mais-novos';

  categorias = [
    { value: '', label: 'Todas as categorias' },
    { value: 'romance', label: 'Romance' },
    { value: 'ficcao', label: 'Ficção' },
    { value: 'terror', label: 'Terror' },
    { value: 'poesia', label: 'Poesia' },
    { value: 'historia', label: 'História' },
    { value: 'tecnologia', label: 'Tecnologia' },
    { value: 'outros', label: 'Outros' },
  ];

  ordenacoes = [
    { value: 'mais-novos', label: 'Mais novos' },
    { value: 'mais-vistos', label: 'Mais vistos' },
    { value: 'alfabetica', label: 'A-Z' },
  ];

  constructor(private router: Router) {}

  onBuscar(): void {
    this.router.navigate(['/'], {
      queryParams: { busca: this.busca || null, categoria: this.categoriaSelecionada || null, ordem: this.ordenacao }
    });
  }

  limparBusca(): void {
    this.busca = '';
    this.onBuscar();
  }

  limparFiltros(): void {
    this.busca = '';
    this.categoriaSelecionada = '';
    this.ordenacao = 'mais-novos';
    this.onBuscar();
  }
}
