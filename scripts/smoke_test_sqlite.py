#!/usr/bin/env python3
"""Smoke test for the IBMEC Research Stars SQLite schema.

Builds data/ibmec.db from V1__init_schema.sql, inserts a tiny fixture covering
every entity from Requisitos.md section 4, and asserts the constraints that
matter most: UNIQUE, CHECK, FK cascade, and the MEC compliance rule
(>=9 validated publications in the last 3 years - RN-01).

Run from the project root:
    python3 scripts/smoke_test_sqlite.py
"""

from __future__ import annotations

import sqlite3
import sys
from datetime import UTC, date, datetime, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SCHEMA = ROOT / "src" / "main" / "resources" / "db" / "migration" / "V1__init_schema.sql"
DB_PATH = ROOT / "data" / "ibmec.db"


def fresh_connection() -> sqlite3.Connection:
    DB_PATH.unlink(missing_ok=True)
    conn = sqlite3.connect(DB_PATH)
    conn.execute("PRAGMA foreign_keys = ON;")
    conn.executescript(SCHEMA.read_text())
    return conn


def seed(conn: sqlite3.Connection) -> dict[str, int]:
    cur = conn.cursor()
    cur.execute(
        "INSERT INTO users(email, password_hash, role) VALUES (?, ?, 'ADMIN')",
        ("admin@ibmec.br", "bcrypt$placeholder"),
    )
    admin_id = cur.lastrowid

    cur.execute(
        "INSERT INTO users(email, password_hash, role) VALUES (?, ?, 'PROFESSOR')",
        ("ana@ibmec.br", "bcrypt$placeholder"),
    )
    prof_user_id = cur.lastrowid

    cur.execute(
        "INSERT INTO courses(name, code) VALUES (?, ?)",
        ("Engenharia de Software", "ENGSW"),
    )
    course_id = cur.lastrowid

    cur.execute(
        """INSERT INTO professors(user_id, name, email, lattes_number, status)
           VALUES (?, ?, ?, ?, 'PENDING')""",
        (prof_user_id, "Ana Souza", "ana@ibmec.br", "1234567890123456"),
    )
    prof_id = cur.lastrowid

    cur.execute(
        "INSERT INTO professor_courses(professor_id, course_id) VALUES (?, ?)",
        (prof_id, course_id),
    )

    # Approve the professor (RF-06).
    cur.execute("UPDATE professors SET status='APPROVED' WHERE id=?", (prof_id,))

    today = date.today()
    now_iso = datetime.now(UTC).replace(tzinfo=None).isoformat(timespec="seconds")
    # 8 publications inside the 3-year window, plus 1 just outside it.
    inside = [today - timedelta(days=30 * (i + 1)) for i in range(8)]
    outside = today - timedelta(days=365 * 3 + 30)
    publications = [(d, "VALIDATED") for d in inside] + [(outside, "VALIDATED")]

    for idx, (pub_date, status) in enumerate(publications):
        cur.execute(
            """INSERT INTO publications
               (title, link, publication_date, professor_id, status, validated_by, validated_at)
               VALUES (?, ?, ?, ?, ?, ?, ?)""",
            (
                f"Paper {idx + 1}",
                f"https://example.org/paper-{idx + 1}",
                pub_date.isoformat(),
                prof_id,
                status,
                admin_id,
                now_iso,
            ),
        )

    conn.commit()
    return {"admin_id": admin_id, "prof_id": prof_id, "course_id": course_id}


def assert_unique_lattes(conn: sqlite3.Connection, prof_id: int) -> None:
    try:
        conn.execute(
            """INSERT INTO professors(user_id, name, email, lattes_number)
               VALUES ((SELECT id FROM users WHERE email='admin@ibmec.br'),
                       'Dup', 'dup@ibmec.br', '1234567890123456')"""
        )
    except sqlite3.IntegrityError:
        return
    raise AssertionError("duplicate lattes_number was accepted")


def assert_status_check(conn: sqlite3.Connection, prof_id: int) -> None:
    try:
        conn.execute(
            """INSERT INTO publications(title, link, publication_date, professor_id, status)
               VALUES ('bad', 'https://x', '2026-01-01', ?, 'BOGUS')""",
            (prof_id,),
        )
    except sqlite3.IntegrityError:
        return
    raise AssertionError("invalid publication status was accepted")


def assert_cascade_delete(prof_id: int) -> None:
    conn = sqlite3.connect(DB_PATH)
    conn.execute("PRAGMA foreign_keys = ON;")
    conn.execute("DELETE FROM professors WHERE id=?", (prof_id,))
    conn.commit()
    remaining = conn.execute(
        "SELECT COUNT(*) FROM publications WHERE professor_id=?", (prof_id,)
    ).fetchone()[0]
    conn.close()
    assert remaining == 0, f"cascade failed: {remaining} publications remain"


def assert_mec_window(conn: sqlite3.Connection, course_id: int) -> None:
    cutoff = (date.today() - timedelta(days=365 * 3)).isoformat()
    row = conn.execute(
        """SELECT COUNT(*) FROM publications p
           JOIN professor_courses pc ON pc.professor_id = p.professor_id
           JOIN professors prof       ON prof.id = p.professor_id
           WHERE pc.course_id = ?
             AND prof.status = 'APPROVED'
             AND p.status = 'VALIDATED'
             AND p.publication_date >= ?""",
        (course_id, cutoff),
    ).fetchone()
    count = row[0]
    assert count == 8, f"expected 8 qualifying publications, got {count}"


def main() -> int:
    conn = fresh_connection()
    ids = seed(conn)

    assert_unique_lattes(conn, ids["prof_id"])
    assert_status_check(conn, ids["prof_id"])
    assert_mec_window(conn, ids["course_id"])
    conn.close()

    # Cascade test mutates state, so it runs last on its own connection.
    assert_cascade_delete(ids["prof_id"])

    print("PASS")
    return 0


if __name__ == "__main__":
    sys.exit(main())
