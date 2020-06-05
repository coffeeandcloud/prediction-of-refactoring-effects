package it.unisa.softwaredependability.model.metrics;

public class Metric<T> {
    private T value;
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
}
