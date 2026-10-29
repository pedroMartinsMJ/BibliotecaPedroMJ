import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Livro, LivroDTO_CREATE } from '../models/livro.model';
import { Usuario, UsuarioDTO_CREATE } from '../models/usuario.model';

const API_URL = '/api';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  getLivros(): Observable<Livro[]> {
    return this.http.get<Livro[]>(`${API_URL}/livros`);
  }

  criarUsuario(dto: UsuarioDTO_CREATE): Observable<Usuario> {
    return this.http.post<Usuario>(`${API_URL}/usuario/create`, dto);
  }
}
