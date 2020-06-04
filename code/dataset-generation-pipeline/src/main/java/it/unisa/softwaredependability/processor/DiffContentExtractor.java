package it.unisa.softwaredependability.processor;

import it.unisa.softwaredependability.model.InMemoryFile;
import it.unisa.softwaredependability.model.Metric;
import it.unisa.softwaredependability.model.MetricResult;
import it.unisa.softwaredependability.processor.metric.MetricProcessor;
import it.unisa.softwaredependability.utils.FileService;
import it.unisa.softwaredependability.utils.LocalStorageService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DiffContentExtractor {

    private String fileEnding = ".java";
    private String repoName;
    private List<MetricProcessor> metricProcessors;
    private final FileService fileService;

    public final static int OLD_REPO_NAME = 0;
    public final static int NEW_REPO_NAME = 1;

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

    public void execute(String commitId) throws IOException, GitAPIException {
        Map<Integer, File> fsMap = extractToFileSystem(commitId);
        calculateMetricForContent(fsMap.get(OLD_REPO_NAME));
        calculateMetricForContent(fsMap.get(NEW_REPO_NAME));
    }

    private Map<Integer, File> extractToFileSystem(String commitId) throws IOException, GitAPIException {
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

        for(DiffEntry d: diffEntries) {
            log.info("Extracting files between '" + d.getOldId().name() + "' and '" + d.getNewId().name() + "'");
            if(d.getChangeType() != DiffEntry.ChangeType.ADD) {
                filesOldCommit.add(new InMemoryFile(repo.getObjectDatabase().open(d.getOldId().toObjectId()).getBytes(), d.getOldPath()));
            }
            filesNewCommit.add(new InMemoryFile(repo.getObjectDatabase().open(d.getNewId().toObjectId()).getBytes(), d.getNewPath()));
        }

        File oldCommitDir = fileService.generateRepositoryCopy(filesOldCommit);
        File newCommitDir = fileService.generateRepositoryCopy(filesNewCommit);
        Map<Integer, File> dirs = new HashMap<>();
        dirs.put(OLD_REPO_NAME, oldCommitDir);
        dirs.put(NEW_REPO_NAME, newCommitDir);
        walk.dispose();
        return dirs;
    }

    private MetricResult calculateMetricForContent(File rootDir) {
        MetricResult r = new MetricResult();
        for(MetricProcessor p: metricProcessors) {
            Metric m = p.calculate(rootDir);
            if(m != null) r.addMetric(m);
        }
        return r;
    }

    public String getFileEnding() {
        return fileEnding;
    }

    public DiffContentExtractor setFileEnding(String fileEnding) {
        this.fileEnding = fileEnding;
        return this;
    }
}
