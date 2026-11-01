import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { CommonModule } from '@angular/common';

export interface SidebarItem {
  label: string;
  icon: string;
  route?: string;
}

export interface SidebarGroup {
  label: string;
  items: SidebarItem[];
}

const ICON_PATHS: Record<string, string> = {
  home: 'M4 10.5 12 3l8 7.5M6 9v11a1.5 1.5 0 0 0 1.5 1.5h3V15h3v6.5h3A1.5 1.5 0 0 0 18 20V9',
  authors:
    'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
  playlist: 'M9 18a3 3 0 1 1-6 0 3 3 0 0 1 6 0zm12-2a3 3 0 1 1-6 0 3 3 0 0 1 6 0zM9 18V5l12-2v13',
  login: 'M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4M10 17l5-5-5-5M15 12H3',
  'user-plus':
    'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8M19 8v6M22 11h-6',
  postar: 'M12 15V3m0 0-4 4m4-4 4 4M4 17v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2',
  library:
    'M4 19.5A2.5 2.5 0 0 1 6.5 17H20M6.5 3H20v18H6.5A2.5 2.5 0 0 1 4 18.5v-13A2.5 2.5 0 0 1 6.5 3z',
  perfil: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8',
  sair: 'M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9',
};

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  @Input() collapsed = false;
  @Input() isAuthenticated = false;
  @Output() closeRequested = new EventEmitter<void>();

  readonly iconPaths = ICON_PATHS;

  constructor(public authService: AuthService, public router: Router) {}

  private readonly exploreItems: SidebarItem[] = [
    { label: 'Início', icon: 'home', route: '/' },
    { label: 'Autores', icon: 'authors', route: '/autores' },
    { label: 'Playlist', icon: 'playlist', route: '/playlist' },
  ];

  private readonly guestItems: SidebarItem[] = [
    { label: 'Entrar', icon: 'login', route: '/login' },
    { label: 'Cadastrar', icon: 'user-plus', route: '/cadastro' },
  ];

  private readonly accountItems: SidebarItem[] = [
    { label: 'Postar livro', icon: 'postar', route: '/postar' },
    { label: 'Minha Biblioteca', icon: 'library', route: '/minha-biblioteca' },
    { label: 'Perfil', icon: 'perfil', route: '/perfil' },
  ];

  // Memoizado: um getter que criasse arrays novos a cada change detection
  // causaria re-render do DOM em ciclo infinito com o *ngFor aninhado.
  private gruposCache: SidebarGroup[] = [];
  private gruposCacheAuth: boolean | null = null;

  get groups(): SidebarGroup[] {
    if (this.gruposCacheAuth !== this.isAuthenticated) {
      this.gruposCacheAuth = this.isAuthenticated;
      this.gruposCache = this.isAuthenticated
        ? [
            { label: 'Explorar', items: this.exploreItems },
            { label: 'Minha conta', items: this.accountItems },
          ]
        : [
            { label: 'Explorar', items: this.exploreItems },
            { label: 'Conta', items: this.guestItems },
          ];
    }
    return this.gruposCache;
  }

  trackByLabel(_: number, item: SidebarItem): string {
    return item.label;
  }

  trackByGroupLabel(_: number, group: SidebarGroup): string {
    return group.label;
  }

  get userName(): string {
    const usuario = this.authService.getUsuario();
    return usuario?.nome || 'Leitor';
  }

  get userEmail(): string {
    return this.authService.getUsuario()?.email || '';
  }

  get userInitial(): string {
    return this.userName.charAt(0).toUpperCase();
  }

  isExactRoute(route?: string): boolean {
    return route === '/';
  }

  // Objetos estáveis para o routerLinkActiveOptions (evita re-registro a cada CD)
  readonly activeOptionsExact = { exact: true };
  readonly activeOptionsPrefix = { exact: false };

  onItemClick(): void {
    this.closeRequested.emit();
  }

  logout(): void {
    this.authService.logout();
    this.closeRequested.emit();
    this.router.navigate(['/login']);
  }
}
