# IBMEC Research Stars

Backend em Spring Boot e frontend em React/Vite para gerenciar professores,
publicacoes, rankings e relatorios de conformidade por curso.

## Rodar Com Docker

Requisitos:

- Docker
- Docker Compose

Inicie a aplicacao:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Verifique os containers:

```bash
docker compose -f docker-compose.prod.yml ps
```

URLs da aplicacao:

- Frontend: http://localhost:5173
- API do backend: http://localhost:8080/api/v1
- Console H2: http://localhost:8080/h2-console

Login padrao do administrador:

- E-mail: `admin@ibmec.br`
- Senha: `admin123`

Na primeira inicializacao com Docker, o banco de dados e criado apenas com o
usuario administrador. Professores podem se cadastrar pelo frontend e precisam
ser aprovados pelo administrador antes de ficarem ativos em relatorios e
rankings.

## Logs

Logs do backend:

```bash
docker compose -f docker-compose.prod.yml logs -f backend
```

Logs do frontend:

```bash
docker compose -f docker-compose.prod.yml logs -f frontend
```

## Parar A Aplicacao

Pare os containers mantendo o volume do banco de dados:

```bash
docker compose -f docker-compose.prod.yml down
```

## Resetar Os Dados Do Docker

O Docker armazena o banco H2 no volume nomeado `irs-data`.

Para apagar todos os dados locais do Docker e recriar um banco limpo apenas com
o usuario administrador padrao, execute:

```bash
docker compose -f docker-compose.prod.yml down -v
docker compose -f docker-compose.prod.yml up -d --build
```

Esse comando de reset e destrutivo. Ele remove todos os professores, cursos,
publicacoes e solicitacoes pendentes armazenados no banco Docker.
