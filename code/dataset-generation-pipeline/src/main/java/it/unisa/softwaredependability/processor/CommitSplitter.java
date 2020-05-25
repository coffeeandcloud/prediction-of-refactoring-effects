package it.unisa.softwaredependability.processor;

import it.unisa.softwaredependability.model.RepoCommitRange;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class CommitSplitter {

    private List<RepoCommitRange> commits;
    private Logger log = Logger.getLogger(getClass().getName());
    private int repoSplitCount = 10;

    public CommitSplitter(int repoSplitCount) {
        commits = new ArrayList<>();
        this.repoSplitCount = repoSplitCount;
    }

    public List<RepoCommitRange> executeSingle(String repoUrl) {
        List<RepoCommitRange> groupedCommitIds = new ArrayList<>();
        System.out.println("Analyzing github repo '" + repoUrl + "'");
        try {
            RepositoryManager repoManager = RepositoryManager.getInstance();
            Git git = repoManager.openGitWithUrl(repoUrl);
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

    public List<RepoCommitRange> execute(String repoUrl) {
        System.out.println("Analyzing github repo '" + repoUrl + "'");

        try {
            RepositoryManager repoManager = RepositoryManager.getInstance();
            Git git = repoManager.openGitWithUrl(repoUrl);
            Repository repo = git.getRepository();

            Collection<Ref> refs = repo.getRefDatabase().getRefs();
            try {

                Iterable<RevCommit> commitsList = git
                        .log()
                        .add(repo.resolve("refs/heads/master"))
                        .setRevFilter(RevFilter.NO_MERGES).call();



                Long count = 1L;
                String lastCommit = null;
                RepoCommitRange rcr = new RepoCommitRange();
                for(RevCommit commit : commitsList) {
                    int mod = (int)(count % repoSplitCount);
                    String commitHash = commit.getId().getName();

                    lastCommit = commitHash;
                    if(count == 1) {
                        rcr.setStartCommit(commitHash);
                    }
                    if(mod == 0) {
                        rcr.setEndCommit(commitHash);
                        rcr.setRepoUrl(repoUrl);
                        commits.add(rcr);
                        log.info(rcr.toString());
                        rcr = new RepoCommitRange();
                        rcr.setStartCommit(commitHash);
                    }
                    count++;
                }
                if(rcr.getEndCommit() == null) rcr.setEndCommit(lastCommit);
                log.info("Had " + count + " commits");
            } catch(Exception e) {
                e.printStackTrace();
            }
            git.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        return commits;
    }
}
