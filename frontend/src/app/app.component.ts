import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="navbar">
      <div class="brand">Biblioteca Pedro Martins</div>
      <nav>
        <a routerLink="/home" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Início</a>
        <a routerLink="/livros" routerLinkActive="active">Livros</a>
        <a routerLink="/login" routerLinkActive="active">Login</a>
        <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
      </nav>
    </header>
    <main>
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [
    `
      .navbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.9rem 2rem;
        border-bottom: 1px solid #ccc;
        font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif;
      }
      .brand { font-weight: 700; font-size: 1.25rem; color: #111; }
      nav a {
        margin-left: 1.5rem;
        text-decoration: none;
        color: #333;
        font-size: 0.95rem;
      }
      nav a:hover { color: #0066cc; }
      nav a.active { color: #0066cc; font-weight: 700; }
      main { padding: 1rem 2rem; min-height: 60vh; }
    `,
  ],
})
export class AppComponent {
  title = 'bibliotecaPedroMJ';
}
