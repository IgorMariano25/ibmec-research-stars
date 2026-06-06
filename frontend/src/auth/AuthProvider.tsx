import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { jwtDecode } from 'jwt-decode';
import { useNavigate } from 'react-router-dom';
import {
  TOKEN_STORAGE_KEY,
  setUnauthorizedHandler,
} from '../api/httpClient';
import { authService } from '../api/authService';
import type { LoginRequest, Role } from '../api/types';

interface SessionUser {
  name: string;
  email: string;
  role: Role;
  professorId?: number;
}

interface AuthContextValue {
  user: SessionUser | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<SessionUser>;
  logout: () => void;
}

const USER_STORAGE_KEY = 'irs.user';

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

interface JwtPayload {
  sub?: string;
  email?: string;
  name?: string;
  role?: Role;
  roles?: Role[] | string[];
  authorities?: string[];
  professorId?: number;
}

function deriveUser(token: string, fallback?: Partial<SessionUser>): SessionUser {
  let payload: JwtPayload = {};
  try {
    payload = jwtDecode<JwtPayload>(token);
  } catch {
    payload = {};
  }
  const roleFromPayload =
    payload.role ||
    (payload.roles?.[0] as Role | undefined) ||
    (payload.authorities?.find((a) => a.includes('ADMIN') || a.includes('PROFESSOR')) as
      | Role
      | undefined);
  const normalizedRole = (
    typeof roleFromPayload === 'string' && roleFromPayload.includes('ADMIN')
      ? 'ADMIN'
      : typeof roleFromPayload === 'string' && roleFromPayload.includes('PROFESSOR')
      ? 'PROFESSOR'
      : (roleFromPayload as Role | undefined)
  ) as Role | undefined;

  return {
    name: fallback?.name ?? payload.name ?? payload.sub ?? 'Usuário',
    email: fallback?.email ?? payload.email ?? payload.sub ?? '',
    role: fallback?.role ?? normalizedRole ?? 'PROFESSOR',
    professorId: fallback?.professorId ?? payload.professorId,
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem(TOKEN_STORAGE_KEY),
  );
  const [user, setUser] = useState<SessionUser | null>(() => {
    const raw = localStorage.getItem(USER_STORAGE_KEY);
    if (raw) {
      try {
        return JSON.parse(raw) as SessionUser;
      } catch {
        return null;
      }
    }
    const existingToken = localStorage.getItem(TOKEN_STORAGE_KEY);
    return existingToken ? deriveUser(existingToken) : null;
  });

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    setToken(null);
    setUser(null);
    navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      setToken(null);
      setUser(null);
      localStorage.removeItem(USER_STORAGE_KEY);
      navigate('/login', { replace: true });
    });
  }, [navigate]);

  const login = useCallback(async (credentials: LoginRequest) => {
    const response = await authService.login(credentials);
    localStorage.setItem(TOKEN_STORAGE_KEY, response.token);
    const sessionUser = deriveUser(response.token, {
      name: response.name,
      email: response.email ?? credentials.email,
      role: response.role,
      professorId: response.professorId,
    });
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(sessionUser));
    setToken(response.token);
    setUser(sessionUser);
    return sessionUser;
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      token,
      isAuthenticated: Boolean(token && user),
      login,
      logout,
    }),
    [user, token, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
