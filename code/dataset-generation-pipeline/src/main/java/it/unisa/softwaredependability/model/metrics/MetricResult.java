package it.unisa.softwaredependability.model.metrics;

import com.github.mauricioaniche.ck.CKClassResult;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRow;

import java.util.ArrayList;
import java.util.List;

public class MetricResult {
    private String repository;
    private String commitId;
    private String parentCommitId;
    private String filePath;
    private String committer;
    private String modificationName;
    private String refactoringOperation;
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

    public String getRefactoringOperation() {
        return refactoringOperation;
    }

    public MetricResult setRefactoringOperation(String refactoringOperation) {
        this.refactoringOperation = refactoringOperation;
        return this;
    }


    public String getRepository() {
        return repository;
    }

    public MetricResult setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    public List<Row> toRow() {
        List<Row> rows = new ArrayList<>();
        // Generate row for old commit
        if(!metrics.isEmpty()) {
            Metric<CKClassResult> metric = ((Metric<CKClassResult>) metrics.get(0));
            rows.add(generateRow(metric, parentCommitId));
        }
        return rows;
    }

    private GenericRow generateRow(Metric<CKClassResult> m, String parentCommitId) {
        return new GenericRow(
                new Object[] {
                        repository,
                        commitId,
                        parentCommitId,
                        m.getValue().getFile(),
                        refactoringOperation,
                        modificationName,
                        m.getSide(),
                        m.getValue().getLoc(),
                        m.getValue().getDit(),
                        m.getValue().getWmc(),
                        m.getValue().getCbo(),
                        m.getValue().getLcom(),
                        m.getValue().getRfc(),
                        m.getValue().getNumberOfFields()
                }
        );
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
