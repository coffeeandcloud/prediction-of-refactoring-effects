package it.unisa.softwaredependability.processor;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class RepositoryManager {

    private final String TEMP_DIR;
    private transient Logger log = Logger.getLogger(getClass().getName());
    private String localPath;

    public RepositoryManager() {
        this.TEMP_DIR = "/tmp/repos";
    }

    public RepositoryManager(String tempDir) {
        this.TEMP_DIR = tempDir;
    }

    public Git openGitWithUrl(String repoUrl, String executorId) throws IOException, GitAPIException {
        if(repoUrl == null) throw new IOException("Repository not found.");

        localPath = generateExecutorDependentPath(repoUrl, executorId);
        File repoPath = new File(localPath);

        if(repoPath.exists() && repoPath.isDirectory()) {
            return Git.open(repoPath);
        }
        return cloneRepository(repoUrl, localPath);
    }

    public Repository openGitRepositoryWithUrl(String repoUrl, String executorId) throws IOException, GitAPIException {
        return openGitWithUrl(repoUrl, executorId).getRepository();
    }

    private String generateExecutorDependentPath(String repoUrl, String executorId) {
        String localDir = new StringBuilder()
                .append(TEMP_DIR).append("/")
                .append(executorId).append("/")
                .append(extractRepoName(repoUrl)).toString();
        return localDir;
    }

    private Git cloneRepository(String repoUrl, String localDir) throws GitAPIException {
        log.info("Cloning repository '" + repoUrl + "' to '" + localDir + "'");
        Git git =  Git.cloneRepository()
                .setURI(repoUrl)
                .setCloneAllBranches(false)
                .setDirectory(new File(localDir))
                .call();
        log.info("Cloning done.");
        return git;
    }

    private String extractRepoName(String repoUrl) {
        String[] split = repoUrl.split("/");
        return split[split.length-1];
    }

    public String getLocalPath() {
        return localPath;
    }
}
