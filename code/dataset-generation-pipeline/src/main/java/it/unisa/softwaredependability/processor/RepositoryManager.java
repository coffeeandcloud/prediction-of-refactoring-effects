package it.unisa.softwaredependability.processor;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class RepositoryManager {

    private final static String TEMP_DIR = "/tmp/repos/";
    private Map<String, String> repos;
    private static RepositoryManager repositoryManager;
    private transient Logger log = Logger.getLogger(getClass().getName());
    private Long idCount = 0L;

    public static RepositoryManager getInstance() {
        if(repositoryManager == null) {
            repositoryManager = new RepositoryManager();
        }
        return repositoryManager;
    }

    public RepositoryManager() {
        repos = new HashMap<>();
    }

    public Git openGitWithUrl(String repoUrl) throws IOException, GitAPIException {
        if(repoUrl == null) throw new IOException("Repository not found.");

        if(!repos.containsKey(repoUrl)) {
            try {
                return cloneRepository(repoUrl);
            } catch (JGitInternalException e) {
                log.info("Repository '"+repoUrl+"' already exists. Using local directory.");
                addRepo(repoUrl, TEMP_DIR + extractRepoName(repoUrl));
            }
        }
        return Git.open(new File(repos.get(repoUrl)));
    }

    public Repository openGitRepositoryWithUrl(String repoUrl) throws IOException, GitAPIException {
        return openGitWithUrl(repoUrl).getRepository();
    }

    private Git cloneRepository(String repoUrl) throws GitAPIException {
        String localDir = TEMP_DIR + extractRepoName(repoUrl);
        log.info("Cloning repository '" + repoUrl + "' to '" + localDir + "'");
        Git git =  Git.cloneRepository()
                .setURI(repoUrl)
                .setCloneAllBranches(false)
                .setDirectory(new File(localDir))
                .call();
        addRepo(repoUrl, localDir);
        log.info("Cloning done.");
        return git;
    }

    private void addRepo(String repoUrl, String localDir) {
        repos.put(repoUrl, localDir);
        idCount++;
    }

    private String extractRepoName(String repoUrl) {
        String[] split = repoUrl.split("/");
        return split[split.length-1];
    }
}
