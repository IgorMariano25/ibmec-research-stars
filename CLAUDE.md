# CLAUDE.md — IBMEC Research Stars

Aplicação web full-stack para gerenciamento de publicações de pesquisa e ranking de professores da IBMEC, com suporte a relatórios de conformidade MEC/CAPES.

## Estrutura do projeto

```
.
├── src/                          # Backend Spring Boot (Java 21)
│   ├── main/java/br/com/ibmec/researchstars/
│   │   ├── auth/                 # Autenticação JWT
│   │   ├── professor/            # Domínio professor
│   │   ├── publication/          # Domínio publicação
│   │   ├── course/               # Domínio curso
│   │   ├── ranking/              # Lógica de ranking
│   │   ├── report/               # Relatórios MEC/CAPES
│   │   ├── user/                 # Entidade de usuário
│   │   └── common/               # Config, CORS, exceções, validação
│   └── main/resources/
│       ├── application.yml
│       └── db/migration/         # Migrations Flyway (V1__init_schema.sql, V2__seed_admin.sql)
├── frontend/                     # SPA React 18 + TypeScript + Vite (ver frontend/CLAUDE.md)
├── data/                         # Arquivo H2 gerado em runtime
├── jenkins/                      # Configuração Jenkins CI/CD
├── Dockerfile                    # Build multi-stage para produção
├── docker-compose.yml            # Ambiente de desenvolvimento local
├── docker-compose.prod.yml       # Ambiente de produção
└── pom.xml
```

## Stack tecnológica

| Camada | Tecnologia |
|--------|-----------|
| Backend | Java 21, Spring Boot 3.3.5 |
| Persistência | Spring Data JPA, Hibernate, H2 (file-based) |
| Migrations | Flyway |
| Segurança | Spring Security 6, JWT (JJWT 0.11.5) |
| Documentação API | OpenAPI/Swagger (`/swagger-ui.html`) |
| Build | Maven 3.9 |
| Frontend | React 18, TypeScript, Vite, MUI v5 |
| CI/CD | Jenkins, Docker Compose |

## Comandos essenciais

### Backend

```bash
# Build completo
mvn clean package

# Rodar em desenvolvimento (hot reload via devtools)
mvn spring-boot:run

# Rodar todos os testes
mvn test

# Build pulando testes
mvn clean package -Dmaven.test.skip=true
```

### Docker Compose (recomendado para dev local)

```bash
# Sobe backend + frontend com hot reload
docker compose up

# Rebuilda as imagens
docker compose up --build

# Para os serviços
docker compose down
```

### URLs em desenvolvimento

| Serviço | URL |
|---------|-----|
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |
| Frontend | http://localhost:5173 |

## Arquitetura do backend

Organização por domínio (professor, publication, course, ranking, report). Cada domínio segue o padrão:

```
{domain}/
├── {Domain}Controller.java       # Endpoints REST
├── {Domain}Service.java          # Lógica de negócio + transações
├── {Domain}Repository.java       # JPA repository
├── {Domain}.java                 # Entidade JPA
├── dto/                          # DTOs de request/response
├── mapper/                       # Mapeamento Entidade ↔ DTO
└── exception/                    # Exceções customizadas do domínio
```

O domínio `professor` contém subpacote `integration/` com gateways que isolam dependências externas (cursos, publicações) via interfaces — use o mesmo padrão ao adicionar novos cross-domain calls.

### Autenticação

- Stateless JWT via `JwtAuthenticationFilter` (executado antes de cada requisição)
- `SecurityConfig` define quais rotas são públicas (`/api/v1/auth/**`, `/h2-console/**`, `/swagger-ui/**`)
- Role-based: `ROLE_ADMIN` e `ROLE_PROFESSOR`
- CORS configurado em `common/config/CorsConfig.java`

### Banco de dados

- H2 file-based em `./data/researchstars` (dev) e `/app/data/researchstars` (Docker)
- Schema gerenciado exclusivamente pelo Flyway — **nunca altere `ddl-auto`** para `create` ou `update`
- Para adicionar/alterar schema, crie um novo arquivo `V{n+1}__descricao.sql` em `db/migration/`

## Configuração (application.yml)

```yaml
spring.datasource.url            # H2 file path
jwt.secret                       # Chave de assinatura JWT (trocar em produção!)
server.port                      # 8080
spring.h2.console.enabled        # true (desabilitar em produção)
spring.jpa.hibernate.ddl-auto    # validate (gerenciado pelo Flyway)
```

Em produção, sobrescreva `jwt.secret` via variável de ambiente `JWT_SECRET`.

## Testes

```bash
# Todos os testes
mvn test

# Filtrando por tag (ex: unit)
mvn test -Dgroups=unit
```

Testes ficam em `src/test/java/br/com/ibmec/researchstars/` organizados por domínio. Use Mockito para unit tests e `@SpringBootTest` para testes de integração. O `ProfessorControllerTest` usa `MockMvc` com `WithMockUser`.

## Convenções

- DTOs sempre passam por mapper — entidades JPA nunca são expostas diretamente na API
- Exceções de domínio são tratadas por `@RestControllerAdvice` específico do domínio (ex: `ProfessorExceptionHandler`)
- Novas migrations Flyway: `V{versão}__{snake_case_descricao}.sql`
- Segurança: valide entrada apenas na borda (Controller/DTO via Bean Validation) — não replique validação nas camadas internas
