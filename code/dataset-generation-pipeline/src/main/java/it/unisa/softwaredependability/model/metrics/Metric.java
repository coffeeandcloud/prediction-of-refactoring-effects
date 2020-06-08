package it.unisa.softwaredependability.model.metrics;

public class Metric<T> {
    private T value;
    private String side;

    public Metric(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public Metric<T> setValue(T value) {
        this.value = value;
        return this;
    }

    public String getSide() {
        return side;
    }

    public Metric<T> setSide(String side) {
        this.side = side;
        return this;
    }
}
