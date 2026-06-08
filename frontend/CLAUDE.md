# CLAUDE.md — Frontend (Research Stars)

SPA React 18 + TypeScript + Vite que consome o backend Spring Boot em `/api/v1`.

## Comandos essenciais

```bash
# Instalar dependências
npm install

# Servidor de desenvolvimento (http://localhost:5173)
npm run dev

# Build de produção (output em dist/)
npm run build

# Type check
npm run lint
```

## Estrutura de diretórios

```
src/
├── api/                  # Serviços HTTP e tipos de resposta da API
│   ├── authService.ts
│   ├── professorService.ts
│   ├── publicationService.ts
│   ├── courseService.ts
│   └── reportService.ts
├── auth/                 # Contexto de autenticação e guards de rota
│   ├── AuthProvider.tsx  # Contexto global de auth (JWT + user info)
│   ├── ProtectedRoute.tsx
│   └── RoleRoute.tsx     # Guard por role (ADMIN / PROFESSOR)
├── components/           # Componentes reutilizáveis (AppLayout, StatusChip, ConfirmDialog, StatCard)
├── features/             # Páginas organizadas por domínio
│   ├── auth/             # LoginPage, RegisterPage
│   ├── professor/        # ProfessorProfilePage, MyPublicationsPage, MyRankingPage, PublicationFormDialog
│   └── admin/            # AdminDashboardPage, AdminProfessorsPage, AdminProfessorDetailPage,
│                         # AdminPublicationsPage, AdminCoursesPage, AdminRankingPage
├── theme/                # Customização MUI
├── utils/                # Helpers
├── App.tsx               # Definição de rotas (React Router 6)
└── main.tsx              # Bootstrap
```

## Stack

| Dependência | Versão | Uso |
|-------------|--------|-----|
| React | 18.3 | UI |
| TypeScript | 5.5 | Tipagem |
| Vite | 5.4 | Build/dev server |
| MUI (Material UI) | v5 | Componentes visuais |
| MUI X Data Grid | v7 | Tabelas |
| TanStack React Query | 5.51 | Cache e fetching de dados |
| Axios | — | HTTP client com interceptores JWT |
| React Router | 6.26 | Roteamento |
| React Hook Form | 7.52 | Formulários |
| Zod | — | Validação de schema nos formulários |
| notistack | 3.0 | Notificações (snackbars) |
| jwt-decode | 4.0 | Decodificar payload do JWT |

## Configuração

Variável de ambiente necessária (arquivo `.env` na raiz de `frontend/`):

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Em Docker Compose, essa variável é passada pelo `docker-compose.yml`.

## Autenticação

- JWT armazenado em `localStorage` com a chave `irs.token`
- `AuthProvider` decodifica o token com `jwt-decode` e expõe `user` (com `role`) via contexto
- O Axios é configurado com um interceptor que injeta o header `Authorization: Bearer {token}` automaticamente
- `ProtectedRoute` redireciona para `/login` se não autenticado; `RoleRoute` redireciona se o role não bater

## Padrões e convenções

- **Fetching de dados:** sempre via React Query (`useQuery` / `useMutation`) — nunca `useEffect` para chamadas de API
- **Formulários:** React Hook Form + resolver Zod para validação
- **Navegação entre features:** `features/admin/` para administradores, `features/professor/` para professores
- **Componentes de layout:** use `AppLayout` como wrapper de páginas autenticadas
- **Feedback ao usuário:** use `notistack` (`enqueueSnackbar`) para sucesso/erro — não use `alert()`
- **Tipos de API:** defina types/interfaces em `api/` junto ao serviço correspondente, não inline nas páginas

## Roles de usuário

| Role | Acesso |
|------|--------|
| `ROLE_ADMIN` | Todas as rotas admin + perfil próprio |
| `ROLE_PROFESSOR` | Perfil, publicações e ranking próprios |

Use `RoleRoute` com o prop `role` para proteger rotas exclusivas de cada role.
