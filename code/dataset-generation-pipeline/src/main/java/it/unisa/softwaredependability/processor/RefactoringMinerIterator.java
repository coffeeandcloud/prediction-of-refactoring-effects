package it.unisa.softwaredependability.processor;

import it.unisa.softwaredependability.model.GitRefactoringCommit;
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

public class RefactoringMinerIterator implements Iterator<GitRefactoringCommit> {

    private Logger log = Logger.getLogger(getClass().getName());

    private AtomicBoolean hasNextValue = new AtomicBoolean(true);
    private Stack<GitRefactoringCommit> commits;

    private GitService gitService;
    private GitHistoryRefactoringMiner miner;
    private Thread miningThread;

    public RefactoringMinerIterator(String repoUrl) {
        gitService = new GitServiceImpl();
        miner = new GitHistoryRefactoringMinerImpl();
        commits = new Stack<>();
        try {
            execute(repoUrl);
        } catch (Exception e) {

        }
        hasNextValue.set(false);
    }

    private void execute(String repoUrl) throws Exception {
        if(repoUrl == null) return;
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
                            commits.push(new GitRefactoringCommit(commitId, repoUrl, refactorings));
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
    public GitRefactoringCommit next() {

        while(true) {
            if(hasNext() && commits.empty()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if(!commits.empty()) {
                log.info("Popping");
                return commits.pop();
            }
            return null;
        }

    }
}
