export interface Usuario {
  id?: string;
  nome: string;
  username: string;
  email: string;
  cpf: string;
  telefone?: string;
  password?: string;
  ativo?: boolean;
  data_cadastro?: string;
  roles?: string[];
}

export interface UsuarioDTO_CREATE {
  nome: string;
  username: string;
  email: string;
  cpf: string;
  telefone?: string;
  password: string;
}

export interface UsuarioDTO_RESPONSE {
  id: string;
  nome: string;
  username: string;
  email: string;
  cpf: string;
  telefone?: string;
  ativo: boolean;
  data_cadastro: string;
  roles: string[];
}
