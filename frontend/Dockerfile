# ---------- Stage 1: build ----------
FROM node:20-alpine AS build
WORKDIR /app

# Cache de dependências
COPY package.json package-lock.json ./
RUN npm ci

# Código fonte e build de produção.
# A API é acessada via same-origin (/api/v1) graças ao proxy do nginx.
ENV VITE_API_BASE_URL=/api/v1
COPY . .
RUN npm run build

# ---------- Stage 2: runtime (nginx) ----------
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
