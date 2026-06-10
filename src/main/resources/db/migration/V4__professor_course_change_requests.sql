CREATE TABLE professor_course_change_requests (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    professor_id        BIGINT      NOT NULL REFERENCES professors(id) ON DELETE CASCADE,
    status              VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUPERSEDED')),
    requested_by_user_id BIGINT     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reviewed_by_user_id  BIGINT             REFERENCES users(id) ON DELETE SET NULL,
    requested_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at         TIMESTAMP
);

CREATE TABLE professor_course_change_request_courses (
    request_id BIGINT NOT NULL REFERENCES professor_course_change_requests(id) ON DELETE CASCADE,
    course_id  BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    PRIMARY KEY (request_id, course_id)
);

CREATE INDEX idx_prof_course_change_prof_status
    ON professor_course_change_requests (professor_id, status);
