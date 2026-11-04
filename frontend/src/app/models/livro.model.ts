export interface Publicador {
  id: string;
  username: string;
}

export interface Livro {
  id?: string;
  titulo: string;
  autorObra: string;
  publicadoPor?: Publicador;
  descricao?: string;
  editora?: string;
  isbn?: string;
  numeroPaginas?: number;
  tipoArquivo?: 'PDF' | 'EPUB';
  dataPublicacao?: string;
  idioma?: string;
  tamanhoBytes?: number;
  temArquivo?: boolean;
  temCapa?: boolean;
  dataCadastro?: string;
  dataUpload?: string;
}

/** Página do Spring Data como o backend a serializa (chaves em inglês). */
export interface Paginacao<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

/** Corpo de POST /api/livros — sem quem publica: o publicador sai do token. */
export interface LivroDTO_CREATE {
  titulo: string;
  autorObra: string;
  descricao?: string;
  editora?: string;
  isbn?: string;
  dataPublicacao?: string;
  numeroPaginas?: number;
  idioma?: string;
}
