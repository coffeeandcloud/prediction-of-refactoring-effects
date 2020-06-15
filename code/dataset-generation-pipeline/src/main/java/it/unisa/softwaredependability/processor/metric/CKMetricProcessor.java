package it.unisa.softwaredependability.processor.metric;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKClassResult;
import it.unisa.softwaredependability.model.metrics.Metric;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class CKMetricProcessor implements MetricProcessor<CKClassResult> {

    private transient Logger log = Logger.getLogger(getClass().getName());

    @Override
    public List<Metric<CKClassResult>> calculate(File rootDir) {
        // maxAtOnce param defines the batch size for CK (not threading, CK is still single-threaded)
        CK ck = new CK(false, 100, true);
        List<Metric<CKClassResult>> classResults = new ArrayList<>();
        ck.calculate(rootDir.getAbsolutePath(), result -> {
            classResults.add(new Metric<>(result));
        });

        return classResults;
    }

    @Override
    public List<Metric<CKClassResult>> calculate(File rootDir, String[] javaFiles) {
        CK ck = new CK(false, 100, true);
        List<Metric<CKClassResult>> classResults = new ArrayList<>();
        String u = UUID.randomUUID().toString();
        ck.calculateWithGivenFiles(rootDir.getAbsolutePath(), javaFiles, result -> {
            classResults.add(new Metric<>(result));
        });

        return classResults;
    }
}
