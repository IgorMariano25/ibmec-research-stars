ALTER TABLE professors RENAME COLUMN lattes_number TO lattes_url;

ALTER TABLE professors ALTER COLUMN lattes_url VARCHAR(1000);

UPDATE professors
SET lattes_url = CASE
    WHEN LOWER(lattes_url) LIKE 'http://%' OR LOWER(lattes_url) LIKE 'https://%' THEN lattes_url
    ELSE 'https://lattes.cnpq.br/' || lattes_url
END;

ALTER TABLE publications
    ADD COLUMN publication_type VARCHAR(40) NOT NULL DEFAULT 'OTHER'
        CHECK (publication_type IN (
            'JOURNAL_ARTICLE',
            'CONFERENCE_PAPER',
            'BOOK_CHAPTER',
            'BOOK',
            'EXPANDED_ABSTRACT',
            'SIMPLE_ABSTRACT',
            'PROCEEDINGS_WORK',
            'OTHER'
        ));

ALTER TABLE publications
    ADD COLUMN abnt_reference VARCHAR(2000) NOT NULL DEFAULT '';
