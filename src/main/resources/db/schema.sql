CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS public.embeddings
(
    content_hash TEXT PRIMARY KEY,
    file_path    TEXT                                NOT NULL,
    embedding    vector(768)                         NOT NULL,
    content      TEXT                                NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    metadata     jsonb                               NOT NULL
);

ALTER TABLE public.embeddings
    OWNER TO postgres;

CREATE INDEX IF NOT EXISTS idx_embeddings_embedding
    ON public.embeddings USING ivfflat (embedding vector_cosine_ops);

