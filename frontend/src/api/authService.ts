import { httpClient } from './httpClient';
import type { LoginRequest, LoginResponse, RegisterRequest, Professor } from './types';

export const authService = {
  async login(payload: LoginRequest): Promise<LoginResponse> {
    const { data } = await httpClient.post<LoginResponse>('/auth/login', payload);
    return data;
  },
  async register(payload: RegisterRequest): Promise<Professor> {
    const { data } = await httpClient.post<Professor>('/auth/register', payload);
    return data;
  },
};
