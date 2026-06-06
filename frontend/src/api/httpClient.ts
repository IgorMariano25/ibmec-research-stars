import axios, { AxiosError } from 'axios';
import type { ApiError } from './types';

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const TOKEN_STORAGE_KEY = 'irs.token';

export const httpClient = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let onUnauthorized: (() => void) | null = null;
export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler;
}

httpClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      if (onUnauthorized) onUnauthorized();
    }
    return Promise.reject(error);
  },
);

export function extractApiError(error: unknown): ApiError | null {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as ApiError | undefined;
    if (data && typeof data === 'object' && 'message' in data) {
      return data;
    }
    return {
      timestamp: new Date().toISOString(),
      status: error.response?.status ?? 0,
      message: error.message || 'Erro de comunicação com o servidor',
    };
  }
  return null;
}

export function getErrorMessage(error: unknown, fallback = 'Ocorreu um erro inesperado'): string {
  const api = extractApiError(error);
  return api?.message || fallback;
}
