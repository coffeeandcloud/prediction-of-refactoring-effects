package it.unisa.softwaredependability.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.stream.Collectors;

public class RepoCommitRange {
    private String startCommit;
    private String endCommit;
    private String repoUrl;
    private List<String> commitIds;

    public RepoCommitRange() {
    }

    public RepoCommitRange(String startCommit, String endCommit, String repoUrl) {
        this.startCommit = startCommit;
        this.endCommit = endCommit;
        this.repoUrl = repoUrl;
    }

    public RepoCommitRange(List<RevCommit> commits, String repoUrl) {
        this.commitIds = commits.stream().map(x -> x.getId().getName()).collect(Collectors.toList());
        this.repoUrl = repoUrl;
    }

    public String getStartCommit() {
        return startCommit;
    }

    public RepoCommitRange setStartCommit(String startCommit) {
        this.startCommit = startCommit;
        return this;
    }

    public String getEndCommit() {
        return endCommit;
    }

    public RepoCommitRange setEndCommit(String endCommit) {
        this.endCommit = endCommit;
        return this;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public RepoCommitRange setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
        return this;
    }

    public List<String> getCommits() {
        return commitIds;
    }

    public RepoCommitRange setCommits(List<RevCommit> commits) {
        this.commitIds = commits.stream().map(x -> x.getId().getName()).collect(Collectors.toList());
        return this;
    }

    @Override
    public String toString() {
        return "RepoCommitRange{" +
                "startCommit='" + startCommit + '\'' +
                ", endCommit='" + endCommit + '\'' +
                ", repoUrl='" + repoUrl + '\'' +
                '}';
    }
}
