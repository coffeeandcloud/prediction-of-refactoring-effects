package it.unisa.softwaredependability.model;

public class GitRefactoringCommit {
    private String commitId;
    private String refactoringType;
    private String repoUrl;

    public GitRefactoringCommit(String commitId, String refactoringType, String repoUrl) {
        this.commitId = commitId;
        this.refactoringType = refactoringType;
        this.repoUrl = repoUrl;
    }

    public GitRefactoringCommit() { }

    public String getCommitId() {
        return commitId;
    }

    public GitRefactoringCommit setCommitId(String commitId) {
        this.commitId = commitId;
        return this;
    }

    public String getRefactoringType() {
        return refactoringType;
    }

    public GitRefactoringCommit setRefactoringType(String refactoringType) {
        this.refactoringType = refactoringType;
        return this;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public GitRefactoringCommit setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
        return this;
    }
}
