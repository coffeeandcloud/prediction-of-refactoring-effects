package it.unisa.softwaredependability.model;

import java.util.ArrayList;
import java.util.List;

public class MetricResult {
    private String commitId;
    private String parentCommitId;
    private String filePath;
    private String committer;
    private String modificationName;
    private List<Metric> metrics;

    public MetricResult() {
        this.metrics = new ArrayList<>();
    }

    public String getCommitId() {
        return commitId;
    }

    public MetricResult setCommitId(String commitId) {
        this.commitId = commitId;
        return this;
    }

    public String getParentCommitId() {
        return parentCommitId;
    }

    public MetricResult setParentCommitId(String parentCommitId) {
        this.parentCommitId = parentCommitId;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public MetricResult setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public String getCommitter() {
        return committer;
    }

    public MetricResult setCommitter(String committer) {
        this.committer = committer;
        return this;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public MetricResult setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
        return this;
    }

    public MetricResult addMetric(Metric m) {
        metrics.add(m);
        return this;
    }

    public String getModificationName() {
        return modificationName;
    }

    public MetricResult setModificationName(String modificationName) {
        this.modificationName = modificationName;
        return this;
    }

    @Override
    public String toString() {
        return "MetricResult{" +
                "commitId='" + commitId + '\'' +
                ", parentCommitId=" + parentCommitId +
                ", filePath='" + filePath + '\'' +
                ", committer='" + committer + '\'' +
                ", modificationName='" + modificationName + '\'' +
                ", metricSize=" + metrics.size() +
                '}';
    }
}
