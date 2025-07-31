ALTER TABLE embeddings
    DROP CONSTRAINT embeddings_pkey;

ALTER TABLE embeddings
    ADD CONSTRAINT embeddings_pk
        UNIQUE (content_hash, source_repo_name);

