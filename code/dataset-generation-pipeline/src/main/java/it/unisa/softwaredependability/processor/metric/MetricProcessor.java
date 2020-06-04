package it.unisa.softwaredependability.processor.metric;


import it.unisa.softwaredependability.model.Metric;

import java.io.File;

public interface MetricProcessor {
    Metric calculate(File rootDir);
}
