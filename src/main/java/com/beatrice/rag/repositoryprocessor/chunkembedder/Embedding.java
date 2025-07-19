package com.beatrice.rag.repositoryprocessor.chunkembedder;

import java.util.Arrays;
import java.util.List;


/**
 * Represents a vector embedding.
 * <p>Instances of this class are immutable: all internal values are deeply copied when accessed or constructed.</p>
 */
public class Embedding {
    private final float[] values;


    public Embedding(float[] values) {
        this.values = values.clone();
    }


    /**
     * Constructs an {@code Embedding} from a list of {@link Number} values.
     * Each number is converted to float.
     *
     * @param values list of numbers to be converted into an embedding
     */
    public Embedding(List<? extends Number> values) {
        float[] res = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            res[i] = values.get(i).floatValue();
        }
        this.values = res;
    }

    public Embedding(Embedding other) {
        this.values = other.values.clone();
    }

    public float[] getValues() {
        return values.clone();
    }


    /**
     * Returns a new {@link Embedding} instance with the vector normalized to unit length.
     * <p>
     *     Normalization is done by dividing each element of the vector by its L2 norm.
     *     If the vector length is zero, a copy of the original vector is returned.
     * </p>
     * @return normalized embedding vector as an {@code Embedding}
     */
    public Embedding normalize() {
        float sum = 0.0f;
        float[] result = new float[this.values.length];
        for (float i : this.values) {
            sum += i * i;
        }
        float vectorLength = (float) Math.sqrt(sum);

        if (vectorLength == 0.0) {
            return new Embedding(this.values.clone());
        }

        for (int i = 0; i < result.length; i++) {
            result[i] = this.values[i] / vectorLength;
        }

        return new Embedding(result);
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
