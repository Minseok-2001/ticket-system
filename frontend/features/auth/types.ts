export interface User {
  id: number;
  email: string;
  name: string;
  phone?: string;
  role: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  name: string;
  phone?: string;
}

export interface TokenResponse {
  accessToken: string;
  tokenType: string;
  sessionId?: string;
}

export interface AuthResponse {
  user: User;
  token: TokenResponse;
}
