package it.unisa.softwaredependability.processor;

import com.github.mauricioaniche.ck.CKClassResult;
import it.unisa.softwaredependability.model.metrics.Metric;
import it.unisa.softwaredependability.model.metrics.MetricResult;
import it.unisa.softwaredependability.processor.metric.MetricProcessor;
import org.apache.spark.SparkEnv;
import org.apache.spark.sql.Row;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.*;
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
import java.util.Set;
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
        List<DiffEntry> diffs = new ArrayList<>();

        try(DiffFormatter diffFormatter = new DiffFormatter(NullOutputStream.INSTANCE)) {
            diffFormatter.setRepository(repo);
            for(DiffEntry diffEntry : diffFormatter.scan(parentCommit, commit)) {
                if(diffEntry.getChangeType() != DiffEntry.ChangeType.DELETE && diffEntry.getNewPath().endsWith(fileEnding)) {
                    diffs.add(diffEntry);
                }
            }
        }

        Set<String> interestingOldFilePaths = diffs.stream()
                .map(d -> d.getOldPath())
                .map(f -> repositoryManager.getLocalPath() + "/" + f).collect(Collectors.toSet());
        Set<String> interestingNewFilePaths = diffs.stream()
                .map(d -> d.getNewPath())
                .map(f -> repositoryManager.getLocalPath() + "/" + f).collect(Collectors.toSet());

        List<Metric> metrics = new ArrayList<>();

        try {
            checkout(git, parentCommit.name());
            metrics.addAll(calculateMetricsInDir(new File(repositoryManager.getLocalPath()), interestingOldFilePaths.stream().collect(Collectors.toList()), LEFT_SIDE));
            checkout(git, commit.name());
            metrics.addAll(calculateMetricsInDir(new File(repositoryManager.getLocalPath()), interestingNewFilePaths.stream().collect(Collectors.toList()), RIGHT_SIDE));
            checkout(git, headCommit.name());

            for(DiffEntry d: diffs) {
                MetricResult result = createMetricResult(d, refactoringOperation);
                result.getMetrics().addAll(filterMatchingMetrics(
                        repositoryManager.getLocalPath() + "/" + d.getOldPath(),
                        repositoryManager.getLocalPath() + "/" + d.getNewPath(),
                        metrics));
                results.add(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        walk.dispose();
        return results;
    }

    private void checkout(Git git, String commitId) throws GitAPIException {
        try {
            git.checkout().setName(commitId).call();
        } catch (CheckoutConflictException e) {
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            git.checkout().setName(commitId).call();
        } catch (InvalidRefNameException e) {
            e.printStackTrace();
        } catch (RefAlreadyExistsException e) {
            e.printStackTrace();
        } catch (RefNotFoundException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    private List<Metric> filterMatchingMetrics(String oldPath, String newPath, List<Metric> metrics) {
        List<Metric> filteredMetrics = new ArrayList<>();
        for(Metric m: metrics) {
            Metric<CKClassResult> ckMetric = (Metric<CKClassResult>) m;

            if(ckMetric.getValue().getFile().equals(oldPath) || ckMetric.getValue().getFile().equals(newPath)) {
                filteredMetrics.add(ckMetric);
            }
        }
        return filteredMetrics;
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

    private List<Metric> calculateMetricsInDir(File rootDir, List<String> javaFiles, String side) {
        String[] files = javaFiles.stream().toArray(String[]::new);
        List<Metric> metrics = new ArrayList<>();
        for(MetricProcessor p: metricProcessors) {
            List<Metric> m = p.calculate(rootDir, files);
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
