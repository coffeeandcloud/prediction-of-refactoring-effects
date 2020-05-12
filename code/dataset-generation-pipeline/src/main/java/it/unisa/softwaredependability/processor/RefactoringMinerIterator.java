package it.unisa.softwaredependability.processor;

import it.unisa.softwaredependability.model.GitRefactoringCommit;
import it.unisa.softwaredependability.model.RepoCommitRange;
import org.apache.commons.io.FileUtils;
import org.apache.spark.SparkEnv;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRow;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class RefactoringMinerIterator implements Iterator<Row> {

    private final static String TEMP_FILE_DIR = "/tmp/repos/";
    private Logger log = Logger.getLogger(getClass().getName());
    Stack<Row> commits;

    private AtomicBoolean hasNextValue = new AtomicBoolean(true);

    public RefactoringMinerIterator(RepoCommitRange range, String branch) {
        log.info("Mining '"+ range.getRepoUrl()+"' on executor '"+SparkEnv.get().executorId() + "'");
        commits  = new Stack<>();

        try {
            execute(range.getRepoUrl(), branch, range.getEndCommit(), range.getStartCommit());
        } catch (Exception e) {

        }
    }

    public static void cleanupTempFiles() throws IOException {
        File f = new File(TEMP_FILE_DIR);
        if(f.exists() && f.isDirectory()) {
            FileUtils.deleteDirectory(f);
        }
    }

    public static List<Row> executeBlocking(RepoCommitRange range) throws Exception {
        if(range == null || range.getRepoUrl() == null) {
            return Collections.emptyList();
        }
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        ExecutorService es = Executors.newSingleThreadExecutor();

        String[] repoName = range.getRepoUrl().split("/");

        String localRepoDir = TEMP_FILE_DIR + repoName[repoName.length-1];
        Repository repo = gitService.cloneIfNotExists(localRepoDir, range.getRepoUrl());

        //System.out.println("Done cloning '" + range.getRepoUrl() + "'");

        final AtomicBoolean isFinished = new AtomicBoolean(false);

        List<Row> commits = Collections.synchronizedList(new ArrayList<>());

        miner.detectBetweenCommits(repo, range.getEndCommit(), range.getStartCommit(), new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                commits.addAll(GitRefactoringCommit.createSmallRow(range.getRepoUrl(), commitId, refactorings));
            }

            @Override
            public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
                //System.out.println("onFinishCalled.");
                isFinished.set(true);
            }
        });

        while(!isFinished.get()) {
            //System.out.println("Sleeping");
            Thread.sleep(100);
        }

        //System.out.println("Returning refactorings: " + commits.size());
        return commits;
    }

    private void execute(String repoUrl, String branch, String startCommit, String endCommit) throws Exception {
        if(repoUrl == null) {
            hasNextValue.set(false);
            return;
        }
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        ExecutorService es = Executors.newSingleThreadExecutor();

        String[] repoName = repoUrl.split("/");

        String localRepoDir = TEMP_FILE_DIR + repoName[repoName.length-1];
        Repository repo = gitService.cloneIfNotExists(localRepoDir, repoUrl);
        log.info("Done cloning '" + repoUrl + "'");

        es.execute(() -> {
            try {
                miner.detectBetweenCommits(repo, startCommit, endCommit, new RefactoringHandler() {
                    @Override
                    public void handle(String commitId, List<Refactoring> refactorings) {
                        commits.addAll(GitRefactoringCommit.createSmallRow(repoUrl, commitId, refactorings));
                    }

                    @Override
                    public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
                        hasNextValue.set(false);
                        System.out.println("onFinishCalled.");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean hasNext() {
        return hasNextValue.get();
    }

    @Override
    public Row next() {
        while(true) {
            if(!hasNext()) {
                log.info("onNext when iterator already finished.");
                return new GenericRow(new Object[]{
                        null, null, null
                });
            }
            if(hasNext() && commits.empty()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if(!commits.empty()) {
                return commits.pop();
            }
        }
    }
}
