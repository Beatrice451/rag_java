package com.beatrice.rag.repositoryprocessor.chunkembedder;

import java.util.Arrays;
import java.util.List;

public class Embedding {
    private final float[] values;

    public Embedding(float[] values) {
        this.values = values;
    }

    public Embedding(List<? extends Number> values) {
        float[] res = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            res[i] = values.get(i).floatValue();
        }
        this.values = res;
    }

    public float[] getValues() {
        return values.clone();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Embedding embedding = (Embedding) o;
        return Arrays.equals(values, embedding.values);
    }

    @Override
    public String toString() {
        return "Embedding{" +
                "values=" + Arrays.toString(values) +
                '}';
    }
}
