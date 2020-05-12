package it.unisa.softwaredependability.model;

public class RepoCommitRange {
    private String startCommit;
    private String endCommit;
    private String repoUrl;

    public RepoCommitRange() {
    }

    public RepoCommitRange(String startCommit, String endCommit, String repoUrl) {
        this.startCommit = startCommit;
        this.endCommit = endCommit;
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

    @Override
    public String toString() {
        return "RepoCommitRange{" +
                "startCommit='" + startCommit + '\'' +
                ", endCommit='" + endCommit + '\'' +
                ", repoUrl='" + repoUrl + '\'' +
                '}';
    }
}
