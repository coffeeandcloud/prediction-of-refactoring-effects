package it.unisa.softwaredependability.processor.metric;

import it.unisa.softwaredependability.model.metrics.Metric;

import java.io.File;
import java.util.List;

public interface MetricProcessor<T> {
    List<Metric<T>> calculate(File rootDir);
    List<Metric<T>> calculate(File rootDir, String[] javaFiles);
}
