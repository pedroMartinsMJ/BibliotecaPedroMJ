export interface Usuario {
  id?: string;
  nome: string;
  username: string;
  email: string;
  ativo?: boolean;
  data_cadastro?: string;
  roles?: string[];
}

export interface UsuarioDTO_CREATE {
  username: string;
  password: string;
  nome: string;
  email: string;
}

export interface UsuarioDTO_RESPONSE {
  id: string;
  nome: string;
  username: string;
  email: string;
  ativo: boolean;
  data_cadastro: string;
  roles: string[];
}
