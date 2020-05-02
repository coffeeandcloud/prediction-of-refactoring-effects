package it.unisa.softwaredependability.processor;

import it.unisa.softwaredependability.model.GitRefactoringCommit;
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

import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class RefactoringMinerIterator implements Iterator<Row> {

    private Logger log = Logger.getLogger(getClass().getName());

    private AtomicBoolean hasNextValue = new AtomicBoolean(true);
    private Stack<Row> commits;

    private GitService gitService;
    private GitHistoryRefactoringMiner miner;
    private Thread miningThread;

    public RefactoringMinerIterator(String repoUrl) {
        log.info("Mining '"+repoUrl+"' on executor '"+SparkEnv.get().executorId() + "'");
        gitService = new GitServiceImpl();
        miner = new GitHistoryRefactoringMinerImpl();
        commits = new Stack<>();
        try {
            execute(repoUrl);
        } catch (Exception e) {

        }
    }

    private void execute(String repoUrl) throws Exception {
        if(repoUrl == null) {
            hasNextValue.set(false);
            return;
        }
        String[] repoName = repoUrl.split("/");

        String localRepoDir = "/tmp/" + repoName[repoName.length-1];
        Repository repo = gitService.cloneIfNotExists(localRepoDir, repoUrl);
        log.info("Done cloning '" + repoUrl + "'");

        miningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    miner.detectAll(repo, null, new RefactoringHandler() {
                        @Override
                        public void handle(String commitId, List<Refactoring> refactorings) {
                            commits.addAll(GitRefactoringCommit.createRow(commitId, repoUrl, refactorings));
                        }

                        @Override
                        public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
                            hasNextValue.set(false);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        miningThread.start();
    }

    @Override
    public boolean hasNext() {
        return hasNextValue.get();
    }

    @Override
    public Row next() {
        while(true) {
            if(hasNext() && commits.empty()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if(!commits.empty()) {
                return commits.pop();
            }
            return new GenericRow(new Object[]{
                    null, null, null, null, null, null, null, null
            });
        }
    }
}
