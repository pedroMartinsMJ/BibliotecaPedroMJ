import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page perfil-page">
      <div class="page-head">
        <div>
          <h1 class="page-title">Meu perfil</h1>
          <p class="page-subtitle">Suas informações de acesso à biblioteca</p>
        </div>
      </div>

      <div class="cartao-perfil card-panel" *ngIf="usuario; else vazioTpl">
        <div class="perfil-banner"></div>

        <div class="perfil-corpo">
          <div class="avatar">{{ inicial }}</div>
          <h2 class="perfil-nome">{{ usuario.nome }}</h2>
          <span class="perfil-chip" *ngIf="usuario.roles && usuario.roles.length">{{ usuario.roles.join(' · ') }}</span>

          <dl class="perfil-dados">
            <div class="dado">
              <dt>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8"/></svg>
                Nome
              </dt>
              <dd>{{ usuario.nome }}</dd>
            </div>
            <div class="dado">
              <dt>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 6h16a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1zM3.5 7.5 12 13l8.5-5.5"/></svg>
                E-mail
              </dt>
              <dd>{{ usuario.email }}</dd>
            </div>
            <div class="dado">
              <dt>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M15 7a4 4 0 1 1-4 4v6M8.5 4.5 12 8"/></svg>
                Usuário
              </dt>
              <dd>{{ usuario.username }}</dd>
            </div>
            <div class="dado" *ngIf="usuario.roles && usuario.roles.length">
              <dt>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l7 4v5c0 4.4-3 8-7 9-4-1-7-4.6-7-9V7z"/></svg>
                Permissões
              </dt>
              <dd>{{ usuario.roles.join(', ') }}</dd>
            </div>
          </dl>
        </div>
      </div>

      <ng-template #vazioTpl>
        <div class="empty-state">
          <span class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8"/></svg>
          </span>
          <h3>Carregando perfil…</h3>
          <p>Se você não for redirecionado, faça login novamente.</p>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .perfil-page { max-width: 720px; }
    .cartao-perfil { overflow: hidden; }
    .perfil-banner {
      height: 110px;
      background:
        radial-gradient(circle at 85% 20%, rgba(255,255,255,0.28), transparent 40%),
        var(--brand-gradient);
    }
    .perfil-corpo {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 0 2rem 2rem;
      text-align: center;
    }
    .avatar {
      width: 88px;
      height: 88px;
      margin-top: -44px;
      border-radius: 50%;
      background: var(--brand-gradient);
      color: #fff;
      font-size: 2rem;
      font-weight: 800;
      display: grid;
      place-items: center;
      border: 4px solid #fff;
      box-shadow: var(--shadow-md);
    }
    .perfil-nome {
      margin: 0.9rem 0 0.35rem;
      font-size: 1.35rem;
      font-weight: 800;
      letter-spacing: -0.02em;
      color: var(--ink-900);
    }
    .perfil-chip {
      font-size: 0.72rem;
      font-weight: 700;
      letter-spacing: 0.06em;
      text-transform: uppercase;
      color: var(--brand-600);
      background: var(--brand-soft);
      padding: 0.3rem 0.8rem;
      border-radius: 999px;
    }
    .perfil-dados {
      width: 100%;
      margin: 1.75rem 0 0;
      display: flex;
      flex-direction: column;
      gap: 0.6rem;
      text-align: left;
    }
    .dado {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      padding: 0.85rem 1.1rem;
      background: var(--ink-50);
      border: 1px solid var(--border);
      border-radius: var(--radius-md);
    }
    .dado dt {
      display: flex;
      align-items: center;
      gap: 0.55rem;
      font-size: 0.82rem;
      font-weight: 600;
      color: var(--ink-500);
    }
    .dado dt svg {
      width: 17px;
      height: 17px;
      color: var(--brand-500);
    }
    .dado dd {
      margin: 0;
      font-size: 0.92rem;
      font-weight: 600;
      color: var(--ink-900);
      word-break: break-word;
      text-align: right;
    }
    @media (max-width: 560px) {
      .dado { flex-direction: column; align-items: flex-start; gap: 0.25rem; }
      .dado dd { text-align: left; }
      .perfil-corpo { padding: 0 1.25rem 1.5rem; }
    }
  `]
})
export class PerfilComponent {
  usuario: any = null;

  constructor(private authService: AuthService) {
    this.usuario = this.authService.getUsuario();
  }

  get inicial(): string {
    return this.usuario?.nome ? this.usuario.nome.charAt(0).toUpperCase() : '?';
  }
}
