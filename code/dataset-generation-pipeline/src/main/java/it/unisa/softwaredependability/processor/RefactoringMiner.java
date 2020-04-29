package it.unisa.softwaredependability.processor;

import it.unisa.softwaredependability.model.GitRefactoringCommit;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class RefactoringMiner {

    private GitService gitService;
    private GitHistoryRefactoringMiner miner;

    private Logger logger = Logger.getLogger(RefactoringMiner.class.getName());

    private Map<String, String> clonedRepositories;

    private static RefactoringMiner refactoringMiner;

    public static RefactoringMiner getInstance() {
        if(refactoringMiner == null) refactoringMiner = new RefactoringMiner();
        return refactoringMiner;
    }

    public RefactoringMiner() {
        gitService = new GitServiceImpl();
        miner = new GitHistoryRefactoringMinerImpl();
        clonedRepositories = new HashMap<>();
    }

    public List<GitRefactoringCommit> execute(String repoUrl) {
        try {
            String localRepoDir = "/tmp/" + UUID.randomUUID().toString();
            clonedRepositories.put(repoUrl, localRepoDir);
            Repository repo = gitService.cloneIfNotExists(localRepoDir, repoUrl);
            List<GitRefactoringCommit> commits = new ArrayList<>();
            miner.detectAll(repo, null, new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    commits.add(new GitRefactoringCommit(commitId, repoUrl, refactorings));
                }
            });
            return commits;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public void cleanup() {
        for(String dir: clonedRepositories.values()) {
            try {
                File f = new File(dir);
                if(f.exists() && f.isDirectory()) {
                    f.delete();
                    logger.info("Deleted cloned repository '" + f.getAbsolutePath() + "'");
                }
            } catch (Exception e) {

            }
        }
    }
}
