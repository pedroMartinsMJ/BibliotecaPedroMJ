import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent } from '@angular/common/http';
import { Observable, TimeoutError, throwError } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { Livro, LivroDTO_CREATE, Paginacao } from '../models/livro.model';

const API_URL = '/api';
const TEMPO_LIMITE_MS = 10000;
// Um PDF de dezenas de megabytes em conexão lenta passa dos 10s sem ser falha.
const TEMPO_LIMITE_ENVIO_MS = 300000;

export interface DadosDeIdentidade {
  nomeReal: string;
  cpf: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  getLivros(): Observable<Paginacao<Livro>> {
    return this.http.get<Paginacao<Livro>>(`${API_URL}/livros`).pipe(this.protecaoComum());
  }

  /** A capa não vem em base64 no JSON: o arquivo é servido por esta rota. */
  urlDaCapa(livroId: string): string {
    return `${API_URL}/livros/${livroId}/capa`;
  }

  /**
   * Postagem no contrato novo: multipart com o arquivo do livro, a capa opcional
   * e os metadados, tudo na mesma requisição. Quem publica não está aqui — o
   * backend lê a conta do JWT que o interceptor anexa.
   */
  postarLivro(dados: LivroDTO_CREATE, arquivo: File, capa: File | null): Observable<HttpEvent<Livro>> {
    const corpo = new FormData();
    corpo.append('titulo', dados.titulo);
    corpo.append('autorObra', dados.autorObra);
    this.anexarSePreenchido(corpo, 'descricao', dados.descricao);
    this.anexarSePreenchido(corpo, 'isbn', dados.isbn);
    this.anexarSePreenchido(corpo, 'editora', dados.editora);
    this.anexarSePreenchido(corpo, 'dataPublicacao', dados.dataPublicacao);
    this.anexarSePreenchido(corpo, 'idioma', dados.idioma);
    if (dados.numeroPaginas !== undefined && dados.numeroPaginas !== null) {
      corpo.append('numeroPaginas', String(dados.numeroPaginas));
    }
    corpo.append('arquivo', arquivo, arquivo.name);
    if (capa) {
      corpo.append('capa', capa, capa.name);
    }

    return this.http
      .post<Livro>(`${API_URL}/livros`, corpo, { observe: 'events', reportProgress: true })
      .pipe(
        timeout(TEMPO_LIMITE_ENVIO_MS),
        catchError((err) => {
          if (err instanceof TimeoutError) {
            return throwError(() => new Error('O envio demorou demais e foi interrompido. Tente novamente.'));
          }
          return throwError(() => err);
        })
      );
  }

  /**
   * Nome real e CPF obrigatórios no momento de publicar. Depois disto a conta
   * fica habilitada a postar; o histórico é append-only, então enviar de novo
   * cria uma identidade nova em vez de reescrever a anterior.
   */
  confirmarIdentidade(dados: DadosDeIdentidade): Observable<void> {
    return this.http.post<void>('/usuario/identidade', dados).pipe(this.protecaoComum());
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

  // Campo vazio no multipart chegaria como string vazia e travaria a conversão
  // de datas/números no backend; ausente é tratado como opcional.
  private anexarSePreenchido(form: FormData, campo: string, valor?: string): void {
    if (valor !== undefined && valor !== null && valor.trim() !== '') {
      form.append(campo, valor.trim());
    }
  }
}
