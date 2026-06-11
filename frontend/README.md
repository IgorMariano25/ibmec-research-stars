# IBMEC Research Stars — Frontend

SPA em **React 18 + Vite + TypeScript + Material UI** que consome a API do
**IBMEC Research Stars**. O sistema acompanha a produção científica dos
professores e a conformidade dos cursos com a exigência MEC/CAPES
(**≥ 9 publicações validadas nos últimos 3 anos** por professor).

Há dois perfis de acesso:

- **PROFESSOR** — cadastra/gerencia as próprias publicações, acompanha sua
  posição no ranking e solicita alteração dos cursos em que leciona.
- **ADMIN** — aprova professores, valida/rejeita publicações, gerencia cursos
  e acompanha o dashboard de conformidade e o ranking geral.

---

## Stack

| Categoria          | Tecnologia                                             |
| ------------------ | ------------------------------------------------------ |
| Framework / build  | React 18, TypeScript, Vite 5                           |
| UI                 | Material UI v5, `@mui/x-data-grid`, `@mui/icons-material` |
| Roteamento         | React Router v6                                        |
| Dados / cache      | TanStack Query (React Query) v5                        |
| HTTP               | Axios (interceptors de JWT + tratamento de `401`)      |
| Formulários        | React Hook Form + Zod (`@hookform/resolvers`)          |
| Feedback (toasts)  | notistack                                              |
| Auth               | `jwt-decode`                                           |

---

## Pré-requisitos

- Node.js 18+ e npm
- API do backend rodando (por padrão em `http://localhost:8080/api/v1`)

---

## Como rodar

### 1. Desenvolvimento local (Vite)

```bash
cd frontend
npm install
cp .env.example .env   # ajuste VITE_API_BASE_URL se necessário
npm run dev
```

O servidor sobe em `http://localhost:5173` (Vite HMR). Por padrão consome a API
em `http://localhost:8080/api/v1` — ajuste via `VITE_API_BASE_URL` no `.env`.

### 2. Docker — ambiente de desenvolvimento

A partir da **raiz do repositório** (sobe backend + frontend com hot reload via
bind mount):

```bash
docker compose up
```

- Frontend (Vite): `http://localhost:5173`
- Backend (Spring Boot): `http://localhost:8080`

### 3. Docker — ambiente de produção

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Em produção o frontend é compilado e servido **estaticamente pelo nginx**
(imagem multi-stage em [Dockerfile](Dockerfile)), exposto em
`http://localhost:5173` (porta `80` do container). O nginx também faz **proxy
reverso** de `/api/` para o backend (`http://backend:8080`), então o browser
chama tudo *same-origin* (`/api/v1`), eliminando problemas de CORS — ver
[nginx.conf](nginx.conf).

---

## Scripts

| Script            | Descrição                                      |
| ----------------- | ---------------------------------------------- |
| `npm run dev`     | Servidor de desenvolvimento (Vite HMR)         |
| `npm run build`   | Type-check (`tsc -b`) + build de produção (`dist/`) |
| `npm run preview` | Preview local do build de produção             |
| `npm run lint`    | Type-check com TypeScript (`tsc --noEmit`)     |

---

## Variáveis de ambiente

| Variável            | Default                          | Descrição        |
| ------------------- | -------------------------------- | ---------------- |
| `VITE_API_BASE_URL` | `http://localhost:8080/api/v1`   | Base da API REST |

> No build Docker de produção a variável é fixada em `/api/v1` (same-origin via
> proxy do nginx) — ver [Dockerfile](Dockerfile).

---

## Estrutura do projeto

```
frontend/
├─ Dockerfile            # build multi-stage (node → nginx)
├─ nginx.conf            # SPA fallback + proxy /api -> backend
├─ vite.config.ts        # alias "@" -> src, host/porta, polling
└─ src/
   ├─ api/               # httpClient, types e serviços por domínio
   │  ├─ httpClient.ts        # Axios + interceptors JWT/401 + helpers de erro
   │  ├─ types.ts             # contratos/tipos da API (DTOs, enums, Page<T>)
   │  ├─ authService.ts       # login / register
   │  ├─ professorService.ts  # CRUD professores, aprovação, troca de cursos
   │  ├─ publicationService.ts# CRUD publicações, validar/rejeitar
   │  ├─ courseService.ts     # CRUD cursos
   │  └─ reportService.ts     # conformidade por curso + ranking
   ├─ auth/
   │  ├─ AuthProvider.tsx     # contexto de sessão, login/logout, decode do JWT
   │  └─ guards.tsx           # ProtectedRoute (autenticado) e RoleRoute (por perfil)
   ├─ components/
   │  ├─ AppLayout.tsx        # AppBar + Drawer (navegação por perfil)
   │  ├─ StatusChip.tsx       # chip colorido de status
   │  ├─ StatCard.tsx         # cartão de métrica + ProgressBar
   │  ├─ ConfirmDialog.tsx    # diálogo de confirmação reutilizável
   │  └─ States.tsx           # estados de loading / erro / vazio
   ├─ features/
   │  ├─ auth/                # LoginPage, RegisterPage
   │  ├─ professor/           # perfil, publicações, ranking pessoal, form de publicação
   │  └─ admin/               # dashboard, professores, publicações, cursos, ranking
   ├─ theme/index.ts          # tema MUI (cores IBMEC, tipografia, shape)
   ├─ utils/formHelpers.ts    # formatação de datas (pt-BR) e mapeamento de erros de campo
   ├─ App.tsx                 # definição de rotas
   └─ main.tsx                # bootstrap dos providers (Theme, QueryClient, Router, Auth, Snackbar)
```

---

## Arquitetura

- **Camada de API** (`src/api`): cada domínio tem um *service* que encapsula as
  chamadas Axios. O [httpClient.ts](src/api/httpClient.ts) injeta o token JWT em
  toda requisição e centraliza o tratamento de `401` e a extração de mensagens
  de erro (`extractApiError` / `getErrorMessage`).
- **Estado de servidor** com **React Query**: cada tela usa `useQuery` para
  leitura e `useMutation` para escrita, invalidando as *query keys* afetadas
  após cada operação (ex.: validar publicação invalida `publications`,
  `rankings` e `reports`). Config global em [main.tsx](src/main.tsx):
  `retry: 1`, `refetchOnWindowFocus: false`, `staleTime: 30s`.
- **Formulários** com **React Hook Form + Zod**: validação declarativa no
  cliente e mapeamento dos `fieldErrors` retornados pela API para os campos do
  formulário (`applyApiFieldErrors`).
- **Feedback**: `notistack` para toasts (sucesso/erro), `ConfirmDialog` para
  ações destrutivas (excluir, aprovar, validar, rejeitar).

---

## Autenticação e autorização

- `POST /auth/login` retorna um **JWT**, guardado em `localStorage`
  (chave `irs.token`); os dados de sessão derivados ficam em `irs.user`.
- O token é anexado em todas as chamadas via interceptor Axios
  (`Authorization: Bearer <token>`).
- Resposta **`401`** limpa a sessão e redireciona para `/login`.
- Resposta **`403`** leva à tela "Acesso negado" (`/forbidden`).
- A **role** (`ADMIN` | `PROFESSOR`) é obtida do corpo do login e/ou
  decodificada do payload do JWT (campos `role` / `roles` / `authorities`),
  com normalização tolerante a prefixos como `ROLE_`.
- **Guards de rota**:
  - `ProtectedRoute` — exige usuário autenticado.
  - `RoleRoute allow={[...]}` — exige que a role esteja na lista permitida.
- Após login, o usuário é direcionado conforme o perfil:
  `ADMIN → /admin/dashboard`, `PROFESSOR → /me`.

---

## Rotas e telas

### Públicas

| Rota         | Tela           | Descrição                                                                 |
| ------------ | -------------- | ------------------------------------------------------------------------- |
| `/login`     | `LoginPage`    | Autenticação por e-mail/senha; redireciona conforme a role.               |
| `/register`  | `RegisterPage` | Cadastro de professor (nome, e-mail, URL Lattes, senha, cursos). Cria conta com status **PENDENTE**. |
| `/forbidden` | —              | Tela 403 (acesso negado).                                                 |
| `*`          | —              | Tela 404.                                                                 |

### Professor (`PROFESSOR`)

| Rota               | Tela                     | Descrição                                                                                          |
| ------------------ | ------------------------ | -------------------------------------------------------------------------------------------------- |
| `/me`              | `ProfessorProfilePage`   | Dados pessoais, banner de status (pendente/aprovado), cursos vinculados e **solicitação de alteração de cursos**. |
| `/me/publications` | `MyPublicationsPage`     | Tabela das próprias publicações com CRUD (criar/editar/excluir) via `PublicationFormDialog`.        |
| `/me/ranking`      | `MyRankingPage`          | Colocação no ranking, total de publicações validadas (3 anos) e barra de progresso até a meta de 9. |

### Admin (`ADMIN`)

| Rota                    | Tela                        | Descrição                                                                                              |
| ----------------------- | --------------------------- | ------------------------------------------------------------------------------------------------------ |
| `/admin/dashboard`      | `AdminDashboardPage`        | Cards de resumo (cursos, conformidade média, cursos acima do limiar, professores pendentes) e tabela de conformidade por curso, expansível por professor. |
| `/admin/professors`     | `AdminProfessorsPage`       | DataGrid paginado (server-side) com busca e filtro por status; ações de aprovar/detalhar/excluir.       |
| `/admin/professors/:id` | `AdminProfessorDetailPage`  | Detalhe do professor: dados, cursos (editar/aprovar/rejeitar troca), aprovar/excluir e produção científica. |
| `/admin/publications`   | `AdminPublicationsPage`     | DataGrid paginado com filtros (status, professor, busca); diálogo de revisão e ações validar/rejeitar/excluir. |
| `/admin/courses`        | `AdminCoursesPage`          | CRUD de cursos (nome + código) com tratamento de código duplicado (`409`).                              |
| `/admin/ranking`        | `AdminRankingPage`          | Ranking geral por publicações validadas (3 anos), medalhas para o top 3 e estrela para quem atinge a meta. |

> A rota `/me` é acessível também ao `ADMIN`; as demais rotas `/me/*` são
> exclusivas de `PROFESSOR`.

---

## Endpoints consumidos

Base: `VITE_API_BASE_URL` (padrão `http://localhost:8080/api/v1`).

### Autenticação — `authService`

| Método | Endpoint         | Uso                          |
| ------ | ---------------- | ---------------------------- |
| `POST` | `/auth/login`    | Login (retorna JWT)          |
| `POST` | `/auth/register` | Cadastro de professor        |

### Professores — `professorService`

| Método   | Endpoint                                          | Uso                                       |
| -------- | ------------------------------------------------- | ----------------------------------------- |
| `GET`    | `/professors`                                     | Listagem paginada (`page,size,sort,status,q`) |
| `GET`    | `/professors/{id}`                                | Detalhe                                   |
| `GET`    | `/professors/{id}/publications`                   | Publicações do professor                  |
| `GET`    | `/professors/me`                                  | Perfil do professor logado                |
| `POST`   | `/professors/{id}/approve`                        | Aprovar professor                         |
| `POST`   | `/professors/me/course-change-request`            | Solicitar alteração de cursos (professor) |
| `POST`   | `/professors/{id}/course-change-request/approve`  | Aprovar solicitação de cursos (admin)     |
| `POST`   | `/professors/{id}/course-change-request/reject`   | Rejeitar solicitação de cursos (admin)    |
| `PATCH`  | `/professors/{id}`                                | Atualizar professor / cursos              |
| `DELETE` | `/professors/{id}`                                | Excluir professor                         |

### Publicações — `publicationService`

| Método   | Endpoint                      | Uso                                            |
| -------- | ----------------------------- | ---------------------------------------------- |
| `GET`    | `/publications`               | Listagem paginada (`status,professorId,q,...`) |
| `GET`    | `/publications/me`            | Publicações do professor logado                |
| `GET`    | `/publications/{id}`          | Detalhe (usado na revisão admin)               |
| `POST`   | `/publications`               | Criar publicação                               |
| `PATCH`  | `/publications/{id}`          | Editar publicação                              |
| `POST`   | `/publications/{id}/validate` | Validar publicação (admin)                     |
| `POST`   | `/publications/{id}/reject`   | Rejeitar publicação (admin)                    |
| `DELETE` | `/publications/{id}`          | Excluir publicação                             |

### Cursos — `courseService`

| Método   | Endpoint        | Uso             |
| -------- | --------------- | --------------- |
| `GET`    | `/courses`      | Listar cursos   |
| `POST`   | `/courses`      | Criar curso     |
| `PATCH`  | `/courses/{id}` | Editar curso    |
| `DELETE` | `/courses/{id}` | Excluir curso   |

### Relatórios e ranking — `reportService` / `rankingService`

| Método | Endpoint                     | Uso                              |
| ------ | ---------------------------- | -------------------------------- |
| `GET`  | `/reports/course-compliance` | Conformidade por curso (dashboard) |
| `GET`  | `/rankings`                  | Ranking geral                    |
| `GET`  | `/rankings/me`               | Posição do professor logado      |

---

## Modelos de dados e enums

Contratos em [src/api/types.ts](src/api/types.ts). Principais enums:

- **`Role`**: `ADMIN` | `PROFESSOR`
- **`ProfessorStatus`**: `PENDING` | `APPROVED`
- **`PublicationStatus`**: `PENDING` | `VALIDATED` | `REJECTED`
- **`ProfessorCourseChangeStatus`**: `PENDING` | `APPROVED` | `REJECTED` | `SUPERSEDED`
- **`PublicationType`**: `JOURNAL_ARTICLE` (Artigo em periódico),
  `CONFERENCE_PAPER` (Artigo em conferência), `BOOK_CHAPTER` (Capítulo de
  livro), `BOOK` (Livro), `EXPANDED_ABSTRACT` (Resumo expandido),
  `SIMPLE_ABSTRACT` (Resumo simples), `PROCEEDINGS_WORK` (Trabalho em anais),
  `OTHER` (Outro)

Listagens paginadas seguem o formato `Page<T>` do Spring
(`content`, `totalElements`, `totalPages`, `number`, `size`). Os serviços
normalizam respostas que podem vir como array puro, `Page<T>` ou objeto com
`publications`, deixando os componentes agnósticos ao formato.

---

## Regras de negócio refletidas na UI

- **Status visuais** via `StatusChip`:
  - Publicação: `PENDING` (Pendente, *warning*), `VALIDATED` (Validada,
    *success*), `REJECTED` (Rejeitada, *error*).
  - Professor: `PENDING` (Pendente, *warning*), `APPROVED` (Aprovado, *success*).
- **Meta MEC**: cada professor deve ter **≥ 9 publicações validadas nos últimos
  3 anos** (a partir de 1º de janeiro de três anos atrás). Exibida como barra de
  progresso em `/me/ranking` e como estrela no ranking admin.
- **Conformidade por curso** (dashboard): percentual de professores aprovados
  que atingem a meta; chip **"Ok / Atenção"** com **limiar de 50%**.
- **Cadastro pendente**: ao se registrar, o professor entra como `PENDING`; pode
  cadastrar publicações desde já, mas elas só contam nos relatórios após a
  **aprovação do admin**.
- **Validação de publicação** (admin): o botão "Validar" exige um **link (URL)
  válido**; o diálogo de revisão mostra as publicações já validadas do mesmo
  professor para comparação. Validar/rejeitar invalida ranking e relatórios.
- **Re-validação ao editar**: editar uma publicação já `VALIDATED` exibe aviso
  de que ela voltará a `PENDING` e precisará ser validada novamente.
- **Solicitação de troca de cursos**: professor solicita em `/me`; admin
  aprova/rejeita em `/admin/professors/:id`. Enquanto há solicitação pendente,
  o botão de nova solicitação fica desabilitado.
- **Tratamento de `409`** em cadastros (e-mail/Lattes já usados, código de curso
  duplicado), com mensagem amigável e marcação dos campos.

---

## Tema e design

Tema MUI customizado em [src/theme/index.ts](src/theme/index.ts) com a
identidade visual IBMEC:

- **Primária** `#002555` (azul institucional), **secundária/atenção** `#F5AC00`
  (amarelo), **info** `#1245FF`.
- `borderRadius: 10`, tipografia Roboto, botões sem `text-transform` e sem
  elevação.
- Layout responsivo: `AppLayout` usa Drawer permanente em desktop e temporário
  (hambúrguer) em telas menores; a navegação muda conforme o perfil.

---

## Build de produção

```bash
npm run build      # gera artefatos estáticos em dist/
npm run preview    # serve o build localmente para conferência
```

O conteúdo de `dist/` pode ser servido por qualquer servidor estático (nginx,
Azure Static Web Apps, etc.). Em SPAs, lembre-se de redirecionar todas as rotas
para `index.html` (o [nginx.conf](nginx.conf) já faz isso via
`try_files ... /index.html`).
