export interface Livro {
  id?: string;
  titulo: string;
  autor: string;
  descricao?: string;
  editora?: string;
  isbn?: string;
  numero_paginas?: number;
  tipo_arquivo?: 'PDF' | 'EPUB';
  formato_arquivo?: 'PDF' | 'EPUB';
  status_leitura?: 'LER' | 'LENDO' | 'LEU';
  data_publicacao?: string;
  idioma?: string;
  tamanho_bytes?: number;
  arquivo_key?: string;
  capa_key?: string;
  data_cadastro?: string;
  data_upload?: string;
  ativo?: boolean;
}

export interface LivroDTO_CREATE {
  titulo: string;
  autor: string;
  descricao?: string;
  editora?: string;
  isbn?: string;
  numero_paginas?: number;
  tipo_arquivo?: 'PDF' | 'EPUB';
  idioma?: string;
}
