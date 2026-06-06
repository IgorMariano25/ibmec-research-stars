-- IBMEC Research Stars — schema inicial (H2-compatible)

CREATE TABLE users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'PROFESSOR'))
);

CREATE TABLE courses (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE professors (
    id            BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT      NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    matricula     VARCHAR(100) UNIQUE,
    lattes_number VARCHAR(100) NOT NULL UNIQUE,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                      CHECK (status IN ('PENDING', 'APPROVED')),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE professor_courses (
    professor_id BIGINT NOT NULL REFERENCES professors(id) ON DELETE CASCADE,
    course_id    BIGINT NOT NULL REFERENCES courses(id)    ON DELETE CASCADE,
    PRIMARY KEY (professor_id, course_id)
);

CREATE TABLE publications (
    id               BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title            VARCHAR(500) NOT NULL,
    link             VARCHAR(1000) NOT NULL,
    publication_date DATE         NOT NULL,
    professor_id     BIGINT       NOT NULL REFERENCES professors(id) ON DELETE CASCADE,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING', 'VALIDATED', 'REJECTED')),
    validated_by_user_id BIGINT           REFERENCES users(id) ON DELETE SET NULL,
    validated_at     TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pub_prof_status_date
    ON publications (professor_id, status, publication_date);
