package it.unisa.softwaredependability.processor.metric;

import com.github.mauricioaniche.ck.CK;
import it.unisa.softwaredependability.model.Metric;

import java.io.File;
import java.util.logging.Logger;

public class CKMetricProcessor implements MetricProcessor {

    private transient Logger log = Logger.getLogger(getClass().getName());

    @Override
    public Metric calculate(File rootDir) {
        CK ck = new CK();

        ck.calculate(rootDir.getAbsolutePath(), result -> {
            log.info(result.getClassName());
        });

        return null;
    }
}
