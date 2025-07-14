DROP INDEX IF EXISTS idx_embeddings_embedding;

CREATE INDEX idx_embeddings_embedding
    ON public.embeddings USING ivfflat (embedding vector_cosine_ops);
