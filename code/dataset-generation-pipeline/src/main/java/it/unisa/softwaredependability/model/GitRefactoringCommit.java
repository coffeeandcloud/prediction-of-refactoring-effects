package it.unisa.softwaredependability.model;

import org.refactoringminer.api.Refactoring;

import java.util.List;

public class GitRefactoringCommit {
    private String commitId;
    private String repoUrl;
    private List<Refactoring> refactorings;

    public GitRefactoringCommit(String commitId, String repoUrl, List<Refactoring> refactorings) {
        this.commitId = commitId;
        this.repoUrl = repoUrl;
        this.refactorings = refactorings;
    }

    public GitRefactoringCommit() { }

    public String getCommitId() {
        return commitId;
    }

    public GitRefactoringCommit setCommitId(String commitId) {
        this.commitId = commitId;
        return this;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public GitRefactoringCommit setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
        return this;
    }

    public List<Refactoring> getRefactorings() {
        return refactorings;
    }

    public GitRefactoringCommit setRefactorings(List<Refactoring> refactorings) {
        this.refactorings = refactorings;
        return this;
    }
}
