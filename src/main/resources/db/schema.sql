CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS public.embeddings
(

    content_hash     TEXT                                NOT NULL,
    file_path        TEXT                                NOT NULL,
    line_start       INTEGER                             NOT NULL,
    line_end         INTEGER                             NOT NULL,
    embedding        vector(768)                         NOT NULL,
    content          TEXT                                NOT NULL,
    last_updated     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    metadata         jsonb                               NOT NULL,
    source_repo_name TEXT                                NOT NULL,
    CONSTRAINT embedding_pk
        UNIQUE (content_hash, source_repo_name)
);

ALTER TABLE public.embeddings
    OWNER TO postgres;

CREATE INDEX IF NOT EXISTS idx_embeddings_embedding
    ON public.embeddings USING ivfflat (embedding vector_cosine_ops);

