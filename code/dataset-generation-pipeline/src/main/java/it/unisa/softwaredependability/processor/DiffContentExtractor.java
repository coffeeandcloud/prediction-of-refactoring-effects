package it.unisa.softwaredependability.processor;

import com.github.mauricioaniche.ck.CKClassResult;
import it.unisa.softwaredependability.model.InMemoryFile;
import it.unisa.softwaredependability.model.metrics.Metric;
import it.unisa.softwaredependability.model.metrics.MetricResult;
import it.unisa.softwaredependability.processor.metric.MetricProcessor;
import it.unisa.softwaredependability.service.FileService;
import it.unisa.softwaredependability.service.LocalStorageService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.NullOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DiffContentExtractor {

    private String fileEnding = ".java";
    private String repoName;
    private List<MetricProcessor> metricProcessors;
    private final FileService fileService;

    private File oldCommitDir;
    private File newCommitDir;

    private Logger log = Logger.getLogger(getClass().getName());

    public DiffContentExtractor(String repoName) {
        this.repoName = repoName;
        this.metricProcessors = new ArrayList<>();
        fileService = new LocalStorageService();

    }

    public DiffContentExtractor addMetricProcessor(MetricProcessor processor) {
        metricProcessors.add(processor);
        return this;
    }

    public List<MetricResult> execute(String commitId, String refactoringOperation) throws IOException, GitAPIException {
        List<MetricResult> metricResults = extractToFileSystem(commitId, refactoringOperation);
        for(MetricResult mr: metricResults) {
            for(Metric<CKClassResult> oldMetric: calculateMetricForContent(oldCommitDir)) {
                if(oldMetric.getValue().getFile().contains(mr.getFilePath())) {
                    mr.addOldMetric(oldMetric);
                }
            }

            for(Metric<CKClassResult> newMetric: calculateMetricForContent(newCommitDir)) {
                if(newMetric.getValue().getFile().contains(mr.getFilePath())) {
                    mr.addNewMetric(newMetric);
                }
            }
        }
        oldCommitDir.delete();
        newCommitDir.delete();
        return metricResults;
    }

    private List<MetricResult> extractToFileSystem(String commitId, String refactoringOperation) throws IOException, GitAPIException {
        Git git = RepositoryManager.getInstance().openGitWithUrl(repoName);
        Repository repo = git.getRepository();

        RevWalk walk = new RevWalk(repo);

        RevCommit commit = walk.parseCommit(repo.resolve(commitId));
        RevCommit parentCommit = commit.getParent(0);

        List<DiffEntry> diffEntries = new ArrayList<>();

        try(DiffFormatter diffFormatter = new DiffFormatter(NullOutputStream.INSTANCE)) {
            diffFormatter.setRepository(repo);
            for(DiffEntry diffEntry : diffFormatter.scan(parentCommit, commit)) {
                if(diffEntry.getChangeType() != DiffEntry.ChangeType.DELETE && diffEntry.getNewPath().endsWith(fileEnding)) {
                    diffEntries.add(diffEntry);
                }
            }
        }

        List<InMemoryFile> filesOldCommit = new ArrayList<>();
        List<InMemoryFile> filesNewCommit = new ArrayList<>();

        List<MetricResult> metricResults = new ArrayList<>();

        for(DiffEntry d: diffEntries) {
            log.info("Extracting files between '" + d.getOldId().name() + "' and '" + d.getNewId().name() + "'");
            MetricResult mr = new MetricResult();
            mr.setCommitId(d.getNewId().name());
            mr.setParentCommitId(d.getOldId().name());
            mr.setFilePath(d.getNewPath());
            mr.setModificationName(d.getChangeType().name());
            mr.setRefactoringOperation(refactoringOperation);
            mr.setRepository(repoName);
            if(d.getChangeType() != DiffEntry.ChangeType.ADD) {
                filesOldCommit.add(new InMemoryFile(repo.getObjectDatabase().open(d.getOldId().toObjectId()).getBytes(), d.getOldPath()));
            }
            filesNewCommit.add(new InMemoryFile(repo.getObjectDatabase().open(d.getNewId().toObjectId()).getBytes(), d.getNewPath()));
            metricResults.add(mr);
        }

        oldCommitDir = fileService.generateRepositoryCopy(filesOldCommit);
        newCommitDir = fileService.generateRepositoryCopy(filesNewCommit);
        walk.dispose();
        return metricResults;
    }

    private List<Metric> calculateMetricForContent(File rootDir) {
        List<Metric> metrics = new ArrayList<>();
        for(MetricProcessor p: metricProcessors) {
            List<Metric> m = p.calculate(rootDir);
            if(m != null) metrics.addAll(m);
        }
        return metrics;
    }

    public String getFileEnding() {
        return fileEnding;
    }

    public DiffContentExtractor setFileEnding(String fileEnding) {
        this.fileEnding = fileEnding;
        return this;
    }
}
