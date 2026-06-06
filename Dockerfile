# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache de dependências
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Código fonte e empacotamento
COPY src ./src
RUN mvn -B clean package -Dmaven.test.skip=true

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuário sem privilégios + diretório de dados do H2 (modo arquivo)
RUN groupadd -r appuser \
    && useradd -r -g appuser appuser \
    && mkdir -p /app/data

COPY --from=build /app/target/*.jar app.jar

# Garante que o usuário da aplicação seja dono de /app (incluindo /app/data)
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
