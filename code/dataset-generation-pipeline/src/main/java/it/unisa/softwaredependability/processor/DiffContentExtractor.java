package it.unisa.softwaredependability.processor;

import it.unisa.softwaredependability.model.metrics.Metric;
import it.unisa.softwaredependability.model.metrics.MetricResult;
import it.unisa.softwaredependability.processor.metric.MetricProcessor;
import org.apache.spark.SparkEnv;
import org.apache.spark.sql.Row;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.NullOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DiffContentExtractor {

    private String fileEnding = ".java";
    private String repoName;
    private List<MetricProcessor> metricProcessors;

    private Logger log = Logger.getLogger(getClass().getName());
    private RepositoryManager repositoryManager;

    public final static String LEFT_SIDE = "left";
    public static final String RIGHT_SIDE = "right";

    public DiffContentExtractor(String repoName) {
        this.repoName = repoName;
        this.metricProcessors = new ArrayList<>();
        this.repositoryManager = new RepositoryManager();
    }

    public DiffContentExtractor() {
        this(null);
    }

    public DiffContentExtractor addMetricProcessor(MetricProcessor processor) {
        metricProcessors.add(processor);
        return this;
    }


    public List<MetricResult> execute(String commitId, String refactoringOperation) throws IOException, GitAPIException {
        Git git = repositoryManager.openGitWithUrl(repoName, SparkEnv.get().executorId());
        List<MetricResult> metricResults = calculateByCommitId(commitId, refactoringOperation, git);
        git.close();
        return metricResults;
    }

    public List<MetricResult> executeBatch(List<Row> commitRow) throws IOException, GitAPIException {
        log.info("Executing batch of size " + commitRow.size());
        Git git = null;
        List<MetricResult> metricResults = new ArrayList<>();
        for(Row r: commitRow) {
            repoName = r.getString(0);
            if(git == null) {
                git = repositoryManager.openGitWithUrl(repoName, SparkEnv.get().executorId());
            }
            metricResults.addAll(calculateByCommitId(r.getString(1), flattenList(r.getList(2)), git));
        }
        git.close();
        return metricResults;
    }

    private String flattenList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for(String s: list) {
            sb.append(s).append(",");
        }
        return sb.toString();
    }

    private List<MetricResult> calculateByCommitId(String commitId, String refactoringOperation, Git git) throws IOException, GitAPIException {
        Repository repo = git.getRepository();
        RevWalk walk = new RevWalk(repo);

        RevCommit headCommit = walk.parseCommit(repo.resolve(Constants.HEAD));
        RevCommit commit = walk.parseCommit(repo.resolve(commitId));
        RevCommit parentCommit = commit.getParent(0);

        List<MetricResult> results = new ArrayList<>();

        try(DiffFormatter diffFormatter = new DiffFormatter(NullOutputStream.INSTANCE)) {
            diffFormatter.setRepository(repo);
            for(DiffEntry diffEntry : diffFormatter.scan(parentCommit, commit)) {
                if(diffEntry.getChangeType() != DiffEntry.ChangeType.DELETE && diffEntry.getNewPath().endsWith(fileEnding)) {
                    // TODO add option to filter to avoid duplicates and/or filter by only diff containing files
                    MetricResult metricResult = createMetricResult(diffEntry, refactoringOperation);
                    git.checkout().setName(parentCommit.name()).call();
                    metricResult.getMetrics().addAll(calculateMetricsInDir(new File(repositoryManager.getLocalPath()), LEFT_SIDE));
                    git.checkout().setName(commit.name()).call();
                    metricResult.getMetrics().addAll(calculateMetricsInDir(new File(repositoryManager.getLocalPath()), RIGHT_SIDE));
                    git.checkout().setName(headCommit.name()).call();
                    results.add(metricResult);
                }
            }
        }
        walk.dispose();
        return results;
    }

    private MetricResult createMetricResult(DiffEntry d, String refactoringOperation) {
        log.info("Extracting files between '" + d.getOldId().name() + "' and '" + d.getNewId().name() + "'");
        MetricResult mr = new MetricResult();
        mr.setCommitId(d.getNewId().name());
        mr.setParentCommitId(d.getOldId().name());
        mr.setModificationName(d.getChangeType().name());
        mr.setRefactoringOperation(refactoringOperation);
        mr.setRepository(repoName);
        mr.setFilePath(d.getNewPath());
        return mr;
    }

    private List<Metric> calculateMetricsInDir(File rootDir, String side) {
        List<Metric> metrics = new ArrayList<>();
        for(MetricProcessor p: metricProcessors) {
            List<Metric> m = p.calculate(rootDir);
            if(m != null) metrics.addAll(m);
        }
        return metrics.stream().map(m -> m.setSide(side)).collect(Collectors.toList());
    }

    public String getFileEnding() {
        return fileEnding;
    }

    public DiffContentExtractor setFileEnding(String fileEnding) {
        this.fileEnding = fileEnding;
        return this;
    }
}
