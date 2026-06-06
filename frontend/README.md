# IBMEC Research Stars — Frontend

SPA em **React 18 + Vite + TypeScript + Material UI** para consumir a API do
**IBMEC Research Stars**. Cobre os dois perfis (PROFESSOR / ADMIN), cadastro,
autenticação JWT, gestão de publicações, cursos, validações, ranking e
dashboard de conformidade MEC/CAPES.

## Stack

- React 18 + TypeScript + Vite
- Material UI v5 + `@mui/x-data-grid`
- React Router v6
- TanStack Query (React Query)
- Axios (com interceptors de JWT + tratamento 401)
- React Hook Form + Zod
- notistack (toasts)
- jwt-decode

## Pré-requisitos

- Node.js 18+ e npm

## Setup

```bash
cd frontend
npm install
cp .env.example .env   # ajuste VITE_API_BASE_URL se necessário
npm run dev
```

O servidor sobe em `http://localhost:5173`. Por padrão, o frontend consome a
API em `http://localhost:8080/api/v1` — ajuste via `VITE_API_BASE_URL` no
arquivo `.env`.

## Scripts

| Script           | Descrição                              |
| ---------------- | -------------------------------------- |
| `npm run dev`    | Servidor de desenvolvimento (Vite HMR) |
| `npm run build`  | Build de produção (`dist/`)            |
| `npm run preview`| Preview do build                       |
| `npm run lint`   | Type-check com TypeScript              |

## Variáveis de ambiente

| Variável             | Default                            | Descrição                |
| -------------------- | ---------------------------------- | ------------------------ |
| `VITE_API_BASE_URL`  | `http://localhost:8080/api/v1`     | Base da API REST         |

## Estrutura

```
src/
  api/              # httpClient, types e serviços (auth, professors, publications, courses, reports, rankings)
  auth/             # AuthProvider/context, guards (ProtectedRoute, RoleRoute)
  components/       # AppLayout, StatusChip, ConfirmDialog, States, StatCard, ProgressBar
  features/         # Páginas organizadas por área (auth, professor, admin)
  theme/            # Tema MUI customizado
  utils/            # Helpers (formatação, mapeamento de erros)
  App.tsx           # Rotas
  main.tsx          # Bootstrap (Providers)
```

## Autenticação

- O token JWT recebido em `POST /auth/login` é guardado em `localStorage`
  (chave `irs.token`) e anexado em todas as chamadas via interceptor Axios.
- Resposta `401` limpa a sessão e redireciona para `/login`.
- Resposta `403` exibe tela de "acesso negado" (rota `/forbidden`).
- A role do usuário (`ADMIN` ou `PROFESSOR`) é decodificada do payload do
  token (campos `role`/`roles`/`authorities`) ou do próprio corpo do login.

## Rotas

Públicas: `/login`, `/register`.

Professor:
- `/me` — perfil + banner de status
- `/me/publications` — CRUD das próprias publicações
- `/me/ranking` — posição pessoal e progresso até a meta de 9

Admin:
- `/admin/dashboard` — conformidade por curso
- `/admin/professors` — lista paginada com filtros
- `/admin/professors/:id` — detalhe + produção científica
- `/admin/publications` — todas as publicações com filtros (status, professor, busca)
- `/admin/courses` — CRUD de cursos
- `/admin/ranking` — ranking completo

## Regras de negócio refletidas na UI

- Status com `Chip` colorido:
  `PENDING` (warning), `VALIDATED` (success), `REJECTED` (error),
  `APPROVED` (success) para professores.
- Meta MEC: ≥ 9 publicações validadas nos últimos 3 anos — exibida como
  barra de progresso (`/me/ranking`) e ícone de estrela (ranking admin).
- Conformidade por curso: barras de progresso e indicador
  "Ok / Atenção" (limiar 70%).
- Edição de publicação `VALIDATED` exibe aviso de re-validação.
- Validação de publicação requer link válido (URL).
- Tratamento de `409` em cadastros (e-mail/Lattes/código de curso).

## Build de produção

```bash
npm run build
npm run preview
```

O build gera artefatos estáticos em `dist/`, prontos para serem servidos por
qualquer servidor estático (nginx, Azure Static Web Apps, etc.).
