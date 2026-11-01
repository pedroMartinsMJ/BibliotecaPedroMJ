import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, TimeoutError, throwError } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { Livro, LivroDTO_CREATE } from '../models/livro.model';
import { Usuario, UsuarioDTO_CREATE } from '../models/usuario.model';

const API_URL = '/api';
const TEMPO_LIMITE_MS = 10000;

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  getLivros(): Observable<Livro[]> {
    return this.http.get<Livro[]>(`${API_URL}/livros`).pipe(this.protecaoComum());
  }

  criarUsuario(dto: UsuarioDTO_CREATE): Observable<Usuario> {
    return this.http.post<Usuario>(`${API_URL}/usuario/create`, dto).pipe(this.protecaoComum());
  }

  // Evita requisições penduradas para sempre (backend fora do ar, banco sem resposta)
  private protecaoComum() {
    return (source: Observable<any>) =>
      source.pipe(
        timeout(TEMPO_LIMITE_MS),
        catchError((err) => {
          if (err instanceof TimeoutError) {
            return throwError(() => new Error('O servidor não respondeu a tempo. Verifique se o backend está no ar.'));
          }
          return throwError(() => err);
        })
      );
  }
}
