package it.unisa.softwaredependability.cli;

import com.beust.jcommander.Parameter;

import java.net.URI;

public class JobArgs {

    public enum DeployMode {
        CLUSTER, LOCAL;
    }

    @Parameter(names = {"-i", "--input"}, description = "Input file", required = true)
    private URI inputFile;

    @Parameter(names = {"-co", "--commits-output"}, description = "Output directory for refactoring commits", required = true)
    private URI commitsOutputDir;

    @Parameter(names = {"-mo", "--metrics-output"}, description = "Output directory for metrics", required = false)
    private URI metricsOutputDir;

    @Parameter(names = {"-p", "--parallel-jobs"}, description = "Number of parallel mining jobs per repository")
    private int parallelJobs = 1;

    @Parameter(names = {"-r", "--parallel-repos"}, description = "Number of parallel mining repositories")
    private int parallelRepos = 1;

    @Parameter(names = {"-d", "--deploy-mode"}, description = "Deploy mode (default is 'local')")
    private DeployMode deployMode = DeployMode.LOCAL;

    @Parameter(names = {"--username"}, description = "Github username", required = true)
    private String username;

    @Parameter(names = {"--token"}, description = "Github authentication token", required = true)
    private String token;

    @Parameter(names = {"--branch", "-b"}, description = "Branch to mine refactorings on")
    private String branch;

    @Parameter(names = {"--batch-size", "-bs"}, description = "Size of the batch in which the commits are splitted. Higher batch sizes require more system memory.")
    private int batchSize = 100;

    @Parameter(names = {"--refactoring-mining-only"}, description = "If set to true, only the refactoring mining step is executed", required = false)
    private boolean refactoringMiningOnly = false;

    public URI getInputFile() {
        return inputFile;
    }

    public JobArgs setInputFile(URI inputFile) {
        this.inputFile = inputFile;
        return this;
    }

    public URI getCommitsOutputDir() {
        return commitsOutputDir;
    }

    public JobArgs setCommitsOutputDir(URI commitsOutputDir) {
        this.commitsOutputDir = commitsOutputDir;
        return this;
    }

    public DeployMode getDeployMode() {
        return deployMode;
    }

    public JobArgs setDeployMode(DeployMode deployMode) {
        this.deployMode = deployMode;
        return this;
    }

    public int getParallelJobs() {
        return parallelJobs;
    }

    public JobArgs setParallelJobs(int parallelJobs) {
        this.parallelJobs = parallelJobs;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public JobArgs setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getToken() {
        return token;
    }

    public JobArgs setToken(String token) {
        this.token = token;
        return this;
    }

    public String getBranch() {
        return branch;
    }

    public JobArgs setBranch(String branch) {
        this.branch = branch;
        return this;
    }

    public int getParallelRepos() {
        return parallelRepos;
    }

    public JobArgs setParallelRepos(int parallelRepos) {
        this.parallelRepos = parallelRepos;
        return this;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public JobArgs setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public URI getMetricsOutputDir() {
        return metricsOutputDir;
    }

    public JobArgs setMetricsOutputDir(URI metricsOutputDir) {
        this.metricsOutputDir = metricsOutputDir;
        return this;
    }

    public boolean isRefactoringMiningOnly() {
        return refactoringMiningOnly;
    }

    public JobArgs setRefactoringMiningOnly(boolean refactoringMiningOnly) {
        this.refactoringMiningOnly = refactoringMiningOnly;
        return this;
    }
}
