import { Usuario } from './usuario.model';

export interface Credenciais {
  username: string;
  senha: string;
}

export interface AuthResponse {
  token: string;
  usuario: Usuario;
}

export interface TokenPayload {
  sub?: string;
  username?: string;
  roles?: string[];
  exp?: number;
}
