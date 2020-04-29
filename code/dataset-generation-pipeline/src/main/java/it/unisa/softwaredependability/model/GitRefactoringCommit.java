package it.unisa.softwaredependability.model;

import gr.uom.java.xmi.diff.CodeRange;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRow;
import org.refactoringminer.api.Refactoring;

import java.util.ArrayList;
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

    public List<Row> toRows() {
        List<Row> rows = new ArrayList<>();

        for(Refactoring r: refactorings) {
            rows.add(new GenericRow(
                    new Object[] {
                            repoUrl,
                            commitId,
                            r.getName(),
                            r.toString(),
                            fillSubStructure(r.leftSide()),
                            fillSubStructure(r.rightSide())
                    }));
        }
        return rows;
    }

    private Object[] fillSubStructure(List<CodeRange> side) {
        List<Object> sub = new ArrayList<>();
        for(CodeRange c: side) {
            List<Object> refactoringStruct = new ArrayList<>();
            refactoringStruct.add(c.getFilePath());
            refactoringStruct.add(c.getStartLine());
            refactoringStruct.add(c.getEndLine());
            refactoringStruct.add(c.getStartColumn());
            refactoringStruct.add(c.getEndColumn());
            refactoringStruct.add(c.getCodeElementType().name());
            refactoringStruct.add(c.getDescription());
            refactoringStruct.add(c.getCodeElement());
            sub.add(new GenericRow(refactoringStruct.toArray()));
        }
        return sub.toArray();
    }
}
