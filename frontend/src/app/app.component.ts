import { Component, HostListener } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { AuthService } from './services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, HeaderComponent, CommonModule],
  template: `
    <div class="app-layout" [class.sidebar-collapsed]="sidebarCollapsed">
      <app-sidebar
        [collapsed]="sidebarCollapsed"
        [isAuthenticated]="authService.isAuthenticated()"
        (closeRequested)="onSidebarNavigate()"
      ></app-sidebar>

      <div class="app-shell">
        <app-header (toggleSidebar)="sidebarCollapsed = !sidebarCollapsed"></app-header>
        <main class="app-main">
          <router-outlet></router-outlet>
        </main>
      </div>

      <div
        class="sidebar-backdrop"
        *ngIf="isMobile && !sidebarCollapsed"
        (click)="sidebarCollapsed = true"
      ></div>
    </div>
  `,
  styles: [
    `
      .app-layout {
        min-height: 100vh;
        background: var(--surface-alt);
      }

      .app-shell {
        margin-left: var(--sidebar-width);
        min-height: 100vh;
        display: flex;
        flex-direction: column;
        transition: margin-left 0.28s ease;
      }

      .app-layout.sidebar-collapsed .app-shell {
        margin-left: var(--sidebar-width-collapsed);
      }

      .app-main {
        flex: 1;
        min-width: 0;
      }

      .sidebar-backdrop {
        display: none;
      }

      @media (max-width: 960px) {
        .app-shell,
        .app-layout.sidebar-collapsed .app-shell {
          margin-left: 0;
        }

        .sidebar-backdrop {
          display: block;
          position: fixed;
          inset: 0;
          z-index: 990;
          background: rgba(15, 23, 42, 0.45);
          backdrop-filter: blur(2px);
        }
      }
    `,
  ],
})
export class AppComponent {
  sidebarCollapsed = false;
  isMobile = false;

  constructor(public authService: AuthService) {
    this.isMobile = window.innerWidth <= 960;
    this.sidebarCollapsed = this.isMobile;
  }

  @HostListener('window:resize')
  onResize(): void {
    const mobile = window.innerWidth <= 960;
    if (mobile !== this.isMobile) {
      this.isMobile = mobile;
      this.sidebarCollapsed = mobile;
    }
  }

  onSidebarNavigate(): void {
    if (this.isMobile) {
      this.sidebarCollapsed = true;
    }
  }
}
