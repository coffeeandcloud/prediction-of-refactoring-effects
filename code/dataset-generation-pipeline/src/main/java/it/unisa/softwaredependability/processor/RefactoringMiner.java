package it.unisa.softwaredependability.processor;

import it.unisa.softwaredependability.model.GitRefactoringCommit;
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

public class RefactoringMiner {

    private GitService gitService;
    private GitHistoryRefactoringMiner miner;

    private Logger logger = Logger.getGlobal();

    private static RefactoringMiner refactoringMiner;

    public static RefactoringMiner getInstance() {
        if(refactoringMiner == null) refactoringMiner = new RefactoringMiner();
        return refactoringMiner;
    }

    public RefactoringMiner() {
        gitService = new GitServiceImpl();
        miner = new GitHistoryRefactoringMinerImpl();
    }

    public List<GitRefactoringCommit> execute(String repoUrl) {
        try {
            Repository repo = gitService.cloneIfNotExists("/tmp/" + repoUrl,repoUrl);
            List<GitRefactoringCommit> commits = new ArrayList<>();
            miner.detectAll(repo, "master", new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    GitRefactoringCommit commit = new GitRefactoringCommit();
                    commit.setCommitId(commitId);
                    for(Refactoring r: refactorings) {
                        logger.info(r.toJSON());
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
