# Prompt — Frontend do "IBMEC Research Stars" (React + Material UI)

> Cole este prompt inteiro em um gerador de código (Claude, Cursor, v0, Lovable, etc.).
> Ele é autossuficiente: descreve produto, stack, API, telas, regras e critérios de aceite.

---

## 0. Papel e objetivo

Você é um(a) engenheiro(a) frontend sênior. Construa uma **Single Page Application (SPA)** completa, responsiva e pronta para produção que serve de **front-end para a API REST do "IBMEC Research Stars"**, usando **React** e a biblioteca de componentes **Material UI (MUI)**.

Documentação de referência da stack:
- React — https://react.dev/
- Material UI — https://mui.com/material-ui/

Entregue um projeto **funcional, organizado e tipado**, com código limpo, nomes expressivos e tratamento explícito de carregamento/erro/vazio.

---

## 1. Contexto do produto

O **IBMEC Research Stars** gerencia as publicações de pesquisa dos professores, a validação pelo administrador e relatórios de conformidade com o MEC/CAPES. Há **dois perfis (roles)**:

- **PROFESSOR** — faz autocadastro, mantém (CRUD) **somente as próprias** publicações e vê **apenas a sua própria** posição no ranking.
- **ADMIN** — mantém professores (listar, aprovar, editar, excluir), consulta toda a produção científica, valida/rejeita publicações, gerencia cursos, vê o ranking completo e os dashboards de conformidade.

**Regra-chave do negócio (afeta a UI):** o MEC exige que cada professor tenha **no mínimo 9 publicações validadas nos últimos 3 anos** (janela móvel a partir de *hoje*). Apenas publicações com status `VALIDATED` e `publicationDate` dentro da janela contam. O percentual de conformidade é calculado **por curso**.

---

## 2. Stack técnica (obrigatória/recomendada)

Use exatamente:

- **React 18+** com componentes funcionais e Hooks.
- **Material UI (MUI v5+)**: `@mui/material`, `@mui/icons-material`, `@emotion/react`, `@emotion/styled`. Use o `<ThemeProvider>` com tema customizado. Para tabelas administrativas, use `@mui/x-data-grid` (paginação/ordenação/filtro) **ou** `Table` do MUI core (a critério, mas mantenha consistência).
- **Vite** como ferramenta de build. *(Ajustável: pode ser Next.js se preferir; mantenha SPA.)*
- **TypeScript** (recomendado fortemente — defina os tipos dos DTOs). *(Ajustável para JS, mas prefira TS.)*
- **React Router v6** para roteamento.
- **TanStack Query (React Query)** para data fetching, cache e estados de loading/erro. *(Ajustável.)*
- **Axios** com interceptors para o cliente HTTP (anexar JWT, tratar 401).
- **React Hook Form + Zod** (`@hookform/resolvers`) para formulários e validação.
- **notistack** ou o `Snackbar` do MUI para toasts de sucesso/erro.

Não use bibliotecas de UI concorrentes (Bootstrap, Tailwind, Chakra). Toda a UI é MUI.

---

## 3. Integração com a API

### 3.1 Base e configuração
- Caminho base: **`/api/v1`**, configurável por variável de ambiente `VITE_API_BASE_URL` (default `http://localhost:8080/api/v1`).
- Centralize o cliente HTTP em `src/api/httpClient.ts`.

### 3.2 Autenticação (JWT, stateless)
- `POST /auth/login` recebe credenciais e retorna um **token JWT** (+ role/identidade do usuário).
- Armazene o token (em memória + `localStorage` para persistir sessão) e **anexe `Authorization: Bearer <token>`** em todas as chamadas autenticadas via interceptor.
- Decodifique a role do token (ou do payload de login) para o controle de acesso no front.
- Em resposta **`401`** → limpar sessão e redirecionar para `/login`. Em **`403`** → exibir tela/aviso de "acesso negado".

### 3.3 Formato de erro padrão (`ApiError`)
A API retorna erros num corpo consistente. Trate este shape e exiba mensagens amigáveis (e erros de campo nos formulários):

```json
{
  "timestamp": "2026-05-28T12:00:00Z",
  "status": 400,
  "message": "Validation failed",
  "fieldErrors": [{ "field": "link", "message": "deve ser uma URL válida" }]
}
```

Mapeamento de status para a UI:
- `201` criado · `200` ok · `204` excluído (sem corpo).
- `400` → mostrar erros de validação (inclusive `fieldErrors` no form).
- `401/403` → autenticação/autorização (ver 3.2).
- `404` → "não encontrado".
- `409` → **conflito** (e-mail ou número Lattes duplicado) — destacar no formulário de cadastro/edição.

### 3.4 Paginação
Os endpoints administrativos de "listar tudo" são paginados no estilo Spring Page. Trate o shape:

```json
{ "content": [ /* itens */ ], "totalElements": 0, "totalPages": 0, "number": 0, "size": 20 }
```

Parâmetros suportados: `?page=`, `?size=`, ordenação (`?sort=`), busca textual `?q=` e filtros específicos por recurso.

### 3.5 Tabela de endpoints a consumir

| Método | Caminho | Descrição | Quem usa |
|--------|---------|-----------|----------|
| POST | `/auth/register` | Autocadastro do professor | Público |
| POST | `/auth/login` | Autenticar, retornar token | Público |
| GET | `/courses` | Listar todos os cursos | Autenticado |
| POST | `/courses` | Criar curso | Admin |
| PATCH | `/courses/{id}` | Editar curso | Admin |
| DELETE | `/courses/{id}` | Excluir curso | Admin |
| GET | `/professors` | Listar professores (`?status=`, `?q=`, paginado) | Admin |
| GET | `/professors/{id}` | Consultar professor | Admin |
| GET | `/professors/{id}/publications` | Produção científica completa de um professor | Admin |
| GET | `/professors/me` | Próprio perfil | Professor |
| POST | `/professors/{id}/approve` | Aprovar professor | Admin |
| PATCH | `/professors/{id}` | Editar professor | Admin |
| DELETE | `/professors/{id}` | Excluir professor | Admin |
| POST | `/publications` | Cadastrar a própria publicação | Professor |
| GET | `/publications` | Listar todas (`?status=`, `?professorId=`, `?q=`, paginado) | Admin |
| GET | `/publications/me` | Listar as próprias | Professor |
| GET | `/publications/{id}` | Ver uma publicação | Admin / dono |
| PATCH | `/publications/{id}` | Editar publicação | Dono / Admin |
| POST | `/publications/{id}/validate` | Validar publicação | Admin |
| POST | `/publications/{id}/reject` | Rejeitar publicação | Admin |
| DELETE | `/publications/{id}` | Excluir publicação | Dono / Admin |
| GET | `/reports/course-compliance` | % de professores conformes por curso | Admin |
| GET | `/rankings` | Ranking completo dos professores | Admin |
| GET | `/rankings/me` | Própria posição no ranking | Professor |

### 3.6 Tipos/DTOs (defina em `src/api/types.ts`)

> Os nomes de campo abaixo são o contrato esperado; ajuste se a API real divergir.

```ts
type Role = "ADMIN" | "PROFESSOR";
type ProfessorStatus = "PENDING" | "APPROVED";
type PublicationStatus = "PENDING" | "VALIDATED" | "REJECTED";

interface Course { id: number; name: string; code: string; }

interface Professor {
  id: number;
  name: string;
  email: string;
  lattesNumber: string;
  status: ProfessorStatus;
  courses: Course[];
  createdAt: string; // ISO
}

interface Publication {
  id: number;
  title: string;
  link: string;             // URL válida, obrigatória
  publicationDate: string;  // ISO date
  status: PublicationStatus;
  professorId: number;
  professorName?: string;
  validatedBy?: string | null;
  validatedAt?: string | null;
  createdAt: string;
}

interface RankingEntry {
  rank: number;
  professorId: number;
  name: string;
  validatedPublicationsLast3Years: number;
}

interface MyRanking {
  rank: number;
  validatedPublicationsLast3Years: number;
}

interface CourseCompliance {
  courseId: number;
  courseName: string;
  courseCode: string;
  compliantProfessors: number;     // com >= 9 validadas nos últimos 3 anos
  totalApprovedProfessors: number;
  compliancePercentage: number;    // 0..100
}

// Requests
interface RegisterRequest { name: string; email: string; lattesNumber: string; password: string; courseIds: number[]; }
interface LoginRequest { email: string; password: string; }
interface PublicationРrequest { title: string; link: string; publicationDate: string; }
```

---

## 4. Autenticação e controle de acesso por papel

- Rotas **públicas**: `/login`, `/register`.
- `ProtectedRoute` — exige usuário autenticado; senão redireciona para `/login`.
- `RoleRoute` — restringe por role; acesso indevido → tela "403 / acesso negado".
- O **layout e o menu lateral mudam conforme a role** (itens diferentes para Professor e Admin).
- Após login, redirecionar: Professor → `/me` (perfil/dashboard); Admin → `/admin/dashboard`.

---

## 5. Telas / Páginas

### 5.1 Públicas
- **Login** — e-mail + senha; erros de credencial claros.
- **Cadastro (Professor)** — campos: nome, e-mail, número do Lattes, senha, **seleção múltipla de cursos** (carregar de `GET /courses`). Validação client-side; tratar `409` (e-mail/Lattes duplicado) destacando o campo. Ao concluir, informar que o status inicial é **PENDENTE** e que a aprovação é feita pelo admin.

### 5.2 Área do Professor
- **Meu perfil / dashboard** (`GET /professors/me`): exibir dados e um **banner de status**:
  - `PENDING` → aviso "Seu cadastro aguarda aprovação. Você já pode cadastrar publicações, mas elas só contarão nos relatórios após a aprovação."
  - `APPROVED` → indicador positivo.
  - Mostrar cursos que leciona (chips).
- **Minhas publicações** (`GET /publications/me`): tabela/lista com título, data, link (abre em nova aba) e **chip de status** colorido. Ações: **criar**, **editar**, **excluir** (somente as próprias).
  - Form de publicação: título (obrigatório), **link obrigatório e validado como URL**, data de publicação. Avisar que **editar uma publicação já validada a reabre para validação** (volta a `PENDING`).
  - Confirmar exclusão em diálogo.
- **Minha posição no ranking** (`GET /rankings/me`): card destacando a colocação e a contagem de publicações validadas nos últimos 3 anos. Mostrar **progresso rumo à meta de 9** (ex.: barra de progresso `min(count/9)`), sem expor a lista completa de outros professores.

### 5.3 Área do Admin
- **Dashboard de conformidade** (`GET /reports/course-compliance`): visão principal.
  - Cards/indicadores no topo (ex.: nº de cursos, % médio de conformidade, professores pendentes).
  - **Por curso**: percentual de conformidade + números auditáveis (`compliantProfessors / totalApprovedProfessors`). Use barras de progresso e/ou um gráfico de barras. Realce cursos abaixo de um limiar.
- **Professores** (`GET /professors`): DataGrid paginado com **filtro por status** (`PENDING`/`APPROVED`) e **busca textual** (`?q=` por nome/e-mail/Lattes). Ações por linha: **ver detalhe**, **aprovar** (só quando `PENDING`), **editar**, **excluir** (com confirmação).
- **Detalhe do professor** (`GET /professors/{id}` + `GET /professors/{id}/publications`): dados do professor, cursos, status, e a **produção científica completa** dele em tabela. Botão de aprovar/editar/excluir.
- **Publicações** (`GET /publications`): DataGrid paginado com **filtro por status**, **filtro por professor** (`?professorId=`) e **busca por título** (`?q=`). Ações: **validar**, **rejeitar**, **excluir**. Ao validar/rejeitar, atualizar o cache (React Query `invalidateQueries`) e dar feedback (toast). Bloquear validação de publicação sem link válido.
- **Cursos** (`GET /courses` + CRUD): tabela com criar/editar/excluir (nome, código único). Tratar `409` para código duplicado.
- **Ranking completo** (`GET /rankings`): tabela ordenada (decrescente) por publicações validadas nos últimos 3 anos, com medalhas/realce para o top 3.

---

## 6. Regras de negócio refletidas na UI

- **Status de publicação** com `Chip` colorido consistente: `PENDING` (warning/âmbar), `VALIDATED` (success/verde), `REJECTED` (error/vermelho).
- **Status de professor**: `PENDING` (warning) e `APPROVED` (success) como `Chip`/badge.
- **Meta MEC**: destacar visualmente quem atinge **≥ 9 publicações validadas nos últimos 3 anos** (ex.: estrela/ícone, cor). A "conformidade" é por curso; um professor pode contar em vários cursos.
- **Janela de 3 anos**: ao exibir contagens "que contam", deixe claro no rótulo (ex.: "validadas nos últimos 3 anos"). Não tente recalcular no front — confie nos números da API; o front apenas apresenta.
- **Link obrigatório**: o formulário de publicação valida URL antes de enviar; mas também trate o `400` vindo da API.

---

## 7. Componentes reutilizáveis (em `src/components`)

- `AppLayout` — `AppBar` + `Drawer` lateral responsivo, com menu condicionado à role, nome do usuário e botão de logout.
- `ProtectedRoute`, `RoleRoute`.
- `DataTable` — wrapper sobre DataGrid (paginação server-side, ordenação, slot de filtros/busca).
- `StatusChip` — recebe status de publicação ou de professor e renderiza o `Chip` correto.
- `ConfirmDialog` — confirmação genérica para ações destrutivas.
- `FormDialog` — modal de formulário reutilizável.
- `PublicationForm`, `ProfessorForm`, `CourseForm` — com React Hook Form + Zod.
- `ComplianceCard` / `ProgressBar` — para o dashboard e a meta de 9.
- `StatCard` — indicadores numéricos.
- `LoadingState`, `ErrorState`, `EmptyState` — padronizar feedback (skeletons/spinner, mensagem de erro com retry, estado vazio).
- `SearchField`, `StatusFilter` — controles de filtro reutilizáveis.

---

## 8. UX, validação e feedback

- **Loading**: skeletons nas tabelas/cards; spinners em botões durante submit (desabilitar o botão).
- **Erros**: toast para falhas de ação; mensagens de campo nos formulários a partir de `fieldErrors`.
- **Sucesso**: toast curto e atualização otimista/`invalidateQueries`.
- **Confirmação** em toda exclusão e em aprovar/validar/rejeitar.
- **Acessibilidade**: labels, foco gerenciado em diálogos, contraste adequado, navegação por teclado.
- **Responsividade**: mobile-first; o Drawer vira temporário em telas pequenas; tabelas com rolagem/colunas adaptáveis.
- **Validações client-side** espelhando o backend: campos obrigatórios, e-mail válido, URL válida no link, data não vazia.

---

## 9. Tema MUI

- Crie um tema customizado em `src/theme/`: paleta (primary/secondary coerentes com identidade acadêmica do IBMEC — tons sóbrios de azul/grafite + cores semânticas success/warning/error), tipografia, `shape.borderRadius`, densidade dos componentes.
- Suporte opcional a **modo claro/escuro** com toggle persistido.
- Padronize espaçamentos via `theme.spacing` e componha estilos com `sx`.

---

## 10. Estrutura de pastas sugerida

```
src/
  api/            # httpClient, types, services por recurso (auth, professors, publications, courses, reports, rankings)
  auth/           # AuthProvider/context, useAuth, guards
  components/     # componentes reutilizáveis (seção 7)
  features/       # ou pages/: organizadas por área (auth, professor, admin)
  hooks/          # hooks de dados (useProfessors, usePublications, useCompliance...)
  routes/         # definição de rotas + guards
  theme/          # tema MUI
  utils/          # formatação de datas, helpers
  App.tsx
  main.tsx
```

Use **camelCase** para variáveis/funções, **PascalCase** para componentes/tipos, e nomeie pela intenção (`approvedProfessors`, não `list1`). Evite abreviações (`publication`, não `pub`).

---

## 11. Critérios de aceite (checklist)

O entregável está pronto quando:

- [ ] Login e cadastro funcionam; o cadastro carrega cursos reais e trata `409`.
- [ ] O token JWT é anexado automaticamente; `401` desloga e `403` mostra acesso negado.
- [ ] Rotas e menus são restritos por role; professor não acessa telas de admin.
- [ ] Professor faz CRUD apenas das próprias publicações e vê só o próprio ranking.
- [ ] Admin lista/filtra/pesquisa professores e publicações com paginação server-side.
- [ ] Admin aprova professores, valida/rejeita/exclui publicações, gerencia cursos.
- [ ] Dashboard de conformidade mostra % por curso com os números auditáveis e a meta de ≥ 9.
- [ ] Ranking completo (admin) e ranking pessoal (professor) exibidos corretamente.
- [ ] Estados de loading/erro/vazio padronizados; toasts de sucesso/erro; confirmações em ações destrutivas.
- [ ] Layout responsivo e tema MUI customizado aplicado.
- [ ] Código tipado (TS), organizado conforme a estrutura, com `README` de como rodar (`npm install`, `npm run dev`, `.env` com `VITE_API_BASE_URL`).

---

## 12. Entregáveis

1. O projeto React + MUI completo, rodável com Vite.
2. `README.md` com instruções de setup, variáveis de ambiente e usuários de teste (se houver seed).
3. Tipos dos DTOs e camada de serviços de API isolada.
4. (Opcional) Um `.env.example`.

Comece pela base (Vite + MUI + tema + AuthProvider + roteamento com guards), depois implemente os serviços de API, e então as telas na ordem: autenticação → área do professor → área do admin → dashboards/rankings.
