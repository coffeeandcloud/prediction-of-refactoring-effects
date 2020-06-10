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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.NullOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

    public List<MetricResult> executeBatch(List<Row> commitRow) throws IOException, GitAPIException {
        String batchId = UUID.randomUUID().toString();
        //log.info("Executing batch of size " + commitRow.size() + " (batchId=" +batchId +")");
        Git git = null;
        List<MetricResult> metricResults = new ArrayList<>();
        int it = 0;
        for(Row r: commitRow) {
            //log.info("Iteration " + it + "/"+commitRow.size()+ "(batchId="+batchId+")");
            repoName = r.getString(0);
            git = repositoryManager.openGitWithUrl(repoName, SparkEnv.get().executorId());

            metricResults.addAll(calculateByCommitId(r.getString(1), flattenList(r.getList(2)), git));
            it++;
            git.close();

        }
        //log.info("Closing patch processing (batchId=" +batchId +")");

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
        StoredConfig config = repo.getConfig();
        config.setString("core", null, "autocrlf", "false");
        config.setBoolean("core", null, "ignorecase", true);
        config.save();

        RevWalk walk = new RevWalk(repo);

        ObjectId headCommit = repo.resolve(Constants.HEAD);
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
        walk.dispose();

        Set<String> interestingOldFilePaths = diffs.stream()
                .map(d -> d.getOldPath())
                .map(f -> repositoryManager.getLocalPath() + "/" + f).collect(Collectors.toSet());
        Set<String> interestingNewFilePaths = diffs.stream()
                .map(d -> d.getNewPath())
                .map(f -> repositoryManager.getLocalPath() + "/" + f).collect(Collectors.toSet());

        List<Metric> metrics = new ArrayList<>();

        try {
            //log.info("Checkout first");
            checkout(git, parentCommit.name());
            metrics.addAll(calculateMetricsInDir(new File(repositoryManager.getLocalPath()), interestingOldFilePaths.stream().collect(Collectors.toList()), LEFT_SIDE));
            //log.info("Checkout second");
            checkout(git, commit.name());
            metrics.addAll(calculateMetricsInDir(new File(repositoryManager.getLocalPath()), interestingNewFilePaths.stream().collect(Collectors.toList()), RIGHT_SIDE));
            //log.info("Checkout back to first");
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

        return results;
    }

    private void checkout(Git git, String commitId) throws GitAPIException {
        try {
            // TODO reset the repository
            int count = 0;
            while(git.status().call().hasUncommittedChanges()) {
                log.info("Has uncommitted changes. Resetting... " + commitId);
                git.rm().addFilepattern(".").setCached(false).call();
                git.reset().setMode(ResetCommand.ResetType.HARD).call();
                git.clean().setForce(true).call();

                count++;
                if(count > 3) {
                    log.info("We are stuck in an unresetable state caused by JGit handling the 'core.autocrlf' " +
                            "setting. Some files may be not analyzed.");
                    break;
                }
            }
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
