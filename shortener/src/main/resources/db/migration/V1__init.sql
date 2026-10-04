CREATE TABLE links (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_url TEXT NOT NULL,
    short_code   VARCHAR(16) NOT NULL,
    expires_at   TIMESTAMPTZ,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_links_short_code UNIQUE (short_code),
    CONSTRAINT chk_links_short_code_len CHECK (char_length(short_code) BETWEEN 1 AND 16)
);
