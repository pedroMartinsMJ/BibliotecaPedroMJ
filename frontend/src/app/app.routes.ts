import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { LivrosComponent } from './livros/livros.component';
import { LoginComponent } from './login/login.component';
import { CadastroComponent } from './cadastro/cadastro.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { NotFoundComponent } from './not-found/not-found.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'home', component: HomeComponent },
  { path: 'livros', component: LivrosComponent },
  { path: 'login', component: LoginComponent },
  { path: 'cadastro', component: CadastroComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'perfil', loadComponent: () => import('./features/perfil/perfil.component').then(m => m.PerfilComponent), canActivate: [AuthGuard] },
  { path: 'minha-biblioteca', loadComponent: () => import('./features/minha-biblioteca/minha-biblioteca.component').then(m => m.MinhaBibliotecaComponent), canActivate: [AuthGuard] },
  { path: 'postar', loadComponent: () => import('./features/postar/postar.component').then(m => m.PostarComponent), canActivate: [AuthGuard] },
  { path: 'autores', loadComponent: () => import('./features/autores/autores.component').then(m => m.AutoresComponent) },
  { path: 'playlist', loadComponent: () => import('./features/playlist/playlist.component').then(m => m.PlaylistComponent) },
  { path: '**', component: NotFoundComponent },
];
