package it.unisa.softwaredependability.pipeline;

import java.io.Serializable;
import java.util.Map;

public abstract class Pipeline implements Serializable {

    protected Map<String, Object> config;

    public Pipeline(Map<String, Object> config) {
        this.config = config;
        init(config);
    }

    public abstract void init(Map<String, Object> config);

    public abstract void execute();

    public abstract void shutdown();

}
