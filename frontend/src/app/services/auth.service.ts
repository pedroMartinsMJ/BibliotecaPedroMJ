import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { Credenciais, AuthResponse, TokenPayload, RegisterRequest } from '../models/auth.model';
import { Usuario } from '../models/usuario.model';

const TOKEN_KEY = 'biblioteca_token';
const USUARIO_KEY = 'biblioteca_usuario';
const API_URL = '/api/auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<Usuario | null>(this.getUsuarioStorage());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(credenciais: Credenciais): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/login`, credenciais).pipe(
      tap((response) => {
        const usuario: Usuario = {
          username: response.username,
          nome: response.nome,
          email: response.email,
          roles: response.roles,
        };
        this.setSession(response.token, usuario);
      })
    );
  }

  register(dados: RegisterRequest): Observable<void> {
    return this.http.post<void>(`${API_URL}/register`, dados);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USUARIO_KEY);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = this.decodeToken<TokenPayload>(token);
      return payload.exp ? payload.exp * 1000 > Date.now() : true;
    } catch {
      return false;
    }
  }

  getUsuario(): Usuario | null {
    return this.currentUserSubject.value;
  }

  private setSession(token: string, usuario: Usuario): void {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USUARIO_KEY, JSON.stringify(usuario));
    this.currentUserSubject.next(usuario);
  }

  private getUsuarioStorage(): Usuario | null {
    const raw = localStorage.getItem(USUARIO_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as Usuario;
    } catch {
      return null;
    }
  }

  private decodeToken<T>(token: string): T {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload)) as T;
  }
}
