-- IBMEC Research Stars - initial schema
-- Mirrors the domain model in Requisitos.md section 4.

CREATE TABLE users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    email         TEXT    NOT NULL UNIQUE,
    password_hash TEXT    NOT NULL,
    role          TEXT    NOT NULL CHECK (role IN ('ADMIN', 'PROFESSOR'))
);

CREATE TABLE courses (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT    NOT NULL,
    code TEXT    NOT NULL UNIQUE
);

CREATE TABLE professors (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    name           TEXT    NOT NULL,
    email          TEXT    NOT NULL UNIQUE,
    lattes_number  TEXT    NOT NULL UNIQUE,
    status         TEXT    NOT NULL DEFAULT 'PENDING'
                       CHECK (status IN ('PENDING', 'APPROVED')),
    created_at     TEXT    NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE professor_courses (
    professor_id INTEGER NOT NULL REFERENCES professors(id) ON DELETE CASCADE,
    course_id    INTEGER NOT NULL REFERENCES courses(id)    ON DELETE CASCADE,
    PRIMARY KEY (professor_id, course_id)
);

CREATE TABLE publications (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    title            TEXT    NOT NULL,
    link             TEXT    NOT NULL,
    publication_date TEXT    NOT NULL,
    professor_id     INTEGER NOT NULL REFERENCES professors(id) ON DELETE CASCADE,
    status           TEXT    NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING', 'VALIDATED', 'REJECTED')),
    validated_by     INTEGER          REFERENCES users(id) ON DELETE SET NULL,
    validated_at     TEXT,
    created_at       TEXT    NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX idx_publications_professor_status_date
    ON publications (professor_id, status, publication_date);
