package it.unisa.softwaredependability.processor;

import it.unisa.softwaredependability.model.RepoCommitRange;
import org.apache.spark.SparkEnv;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class CommitSplitter {

    private Logger log = Logger.getLogger(getClass().getName());
    private int repoSplitCount = 10;

    public CommitSplitter(int repoSplitCount) {
        this.repoSplitCount = repoSplitCount;
    }

    public List<RepoCommitRange> executeSingle(String repoUrl) {
        List<RepoCommitRange> groupedCommitIds = new ArrayList<>();
        System.out.println("Analyzing github repo '" + repoUrl + "'");
        try {
            RepositoryManager repoManager = new RepositoryManager();
            Git git = repoManager.openGitWithUrl(repoUrl, SparkEnv.get().executorId());
            Repository repo = git.getRepository();

            RevWalk walk = new RevWalk(repo);
            walk.markStart(walk.parseCommit(repo.resolve(Constants.HEAD)));
            walk.sort(RevSort.COMMIT_TIME_DESC);
            walk.sort(RevSort.TOPO);
            Long count = 0L;
            List<RevCommit> commits = new ArrayList<>();
            for(RevCommit c: walk) {
                commits.add(c);
                count++;
                if(count % repoSplitCount == 0) {
                    groupedCommitIds.add(new RepoCommitRange(commits, repoUrl));
                    commits = new ArrayList<>();
                }
            }
            if(count != 0) {
                groupedCommitIds.add(new RepoCommitRange(commits, repoUrl));
            }
            git.close();
            return groupedCommitIds;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
