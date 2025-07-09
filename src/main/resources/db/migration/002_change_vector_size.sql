-- Change the size of the vector from 1536 to 768

ALTER TABLE embeddings
    ALTER COLUMN embedding TYPE vector(768) USING embedding::vector(768);

