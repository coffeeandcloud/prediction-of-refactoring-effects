package it.unisa.softwaredependability.processor.metric;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKClassResult;
import it.unisa.softwaredependability.model.metrics.Metric;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CKMetricProcessor implements MetricProcessor<CKClassResult> {

    private transient Logger log = Logger.getLogger(getClass().getName());

    @Override
    public List<Metric<CKClassResult>> calculate(File rootDir) {
        CK ck = new CK();
        List<Metric<CKClassResult>> classResults = new ArrayList<>();
        ck.calculate(rootDir.getAbsolutePath(), result -> {
            classResults.add(new Metric<>(result));
        });

        return classResults;
    }
}
