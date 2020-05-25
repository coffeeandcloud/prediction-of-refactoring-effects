package it.unisa.softwaredependability.processor;

import it.unisa.softwaredependability.model.GitRefactoringCommit;
import it.unisa.softwaredependability.model.RepoCommitRange;
import org.apache.spark.SparkEnv;
import org.apache.spark.sql.Row;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class StaticRefactoringMiner {

    private final static String TEMP_FILE_DIR = "/tmp/repos/";
    private static final Logger log = Logger.getLogger("StaticRefactoringMiner");

    public static List<Row> executeBlockingList(RepoCommitRange range) throws Exception {
        if(range == null || range.getRepoUrl() == null) {
            return Collections.emptyList();
        }
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

        String[] repoName = range.getRepoUrl().split("/");

        String localRepoDir = TEMP_FILE_DIR + "/" + SparkEnv.get().executorId() + "/" + repoName[repoName.length-1];
        Repository repo = gitService.cloneIfNotExists(localRepoDir, range.getRepoUrl());

        List<Row> commits = new ArrayList<>();

        log.info("Using repo at '" + localRepoDir + "'");

        log.info("Mining commit range " + range.getCommits().get(0) + " (size: " + range.getCommits().size() + " commits)");
        Long iterCount = 0L;
        log.info("Analyzing commit " + iterCount + "/" + range.getCommits().size() + "('" + range.getRepoUrl() + "')");
        miner.detectAllByCommitIds(repo, range.getCommits(), new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                List<Row> refactoringRows = GitRefactoringCommit.createSmallRow(range.getRepoUrl(), commitId, refactorings);
                if(!refactorings.isEmpty()) log.info("Adding " + refactorings.size());
                commits.addAll(refactoringRows);
            }

            @Override
            public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
                log.info("onFinishCalled.");
            }
        });

        repo.close();
        return commits;
    }
}
