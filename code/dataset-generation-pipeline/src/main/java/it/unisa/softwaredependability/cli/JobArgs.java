package it.unisa.softwaredependability.cli;

import com.beust.jcommander.Parameter;

import java.net.URI;

public class JobArgs {

    public enum DeployMode {
        CLUSTER, LOCAL;
    }

    @Parameter(names = {"-i", "--input"}, description = "Input file", required = true)
    private URI inputFile;

    @Parameter(names = {"-o", "--output"}, description = "Output directory", required = true)
    private URI outputDir;

    @Parameter(names = {"-p", "--parallel-jobs"}, description = "Number of parallel mining jobs")
    private int parallelJobs = 1;

    @Parameter(names = {"-d", "--deploy-mode"}, description = "Deploy mode (default is 'local')")
    private DeployMode deployMode = DeployMode.LOCAL;

    @Parameter(names = {"--username"}, description = "Github username", required = true)
    private String username;

    @Parameter(names = {"--token"}, description = "Github authentication token", required = true)
    private String token;

    @Parameter(names = {"--branch", "-b"}, description = "Branch to mine refactorings on")
    private String branch;

    public URI getInputFile() {
        return inputFile;
    }

    public JobArgs setInputFile(URI inputFile) {
        this.inputFile = inputFile;
        return this;
    }

    public URI getOutputDir() {
        return outputDir;
    }

    public JobArgs setOutputDir(URI outputDir) {
        this.outputDir = outputDir;
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
}
