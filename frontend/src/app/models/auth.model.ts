/** Enviado para POST /api/auth/login */
export interface Credenciais {
  email: string;
  senha: string;
}

/** Enviado para POST /api/auth/register — username público, sem nome civil */
export interface RegisterRequest {
  username: string;
  email: string;
  senha: string;
}

/** Resposta de POST /api/auth/login (flat, como o backend retorna) */
export interface AuthResponse {
  token: string;
  username: string;
  nome: string | null;
  email: string;
  roles: string[];
}

export interface TokenPayload {
  sub?: string;
  username?: string;
  scope?: string;
  exp?: number;
}
