export interface Usuario {
  id?: string;
  username: string;
  email: string;
  nome?: string | null;
  ativo?: boolean;
  data_cadastro?: string;
  roles?: string[];
}

export interface UsuarioDTO_CREATE {
  username: string;
  password: string;
  email: string;
}

export interface UsuarioDTO_RESPONSE {
  id: string;
  username: string;
  nome: string | null;
  email: string;
  ativo: boolean;
  data_cadastro: string;
  roles: string[];
}
