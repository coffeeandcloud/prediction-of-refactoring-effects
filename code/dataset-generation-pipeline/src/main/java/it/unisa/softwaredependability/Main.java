package it.unisa.softwaredependability;

import it.unisa.softwaredependability.cli.CliParser;
import it.unisa.softwaredependability.cli.JobArgs;
import it.unisa.softwaredependability.pipeline.DatasetExtractionPipeline;
import it.unisa.softwaredependability.pipeline.RefactoringMiningPipeline;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Main {

    static Logger logger = Logger.getGlobal();

    public static void main(String[] args) {

        JobArgs jobArgs = CliParser.parse(args);

        //executeRepositoryExtractionPipeline();
        executeRefactoringCommitPipeline(jobArgs);
    }

    static void executeRepositoryExtractionPipeline() {
        Map<String, Object> config = new HashMap<>();
        config.put("projectSourceFile", "/Volumes/Elements/github_archive/github-2019-06-01/projects.csv");
        config.put("commitSourceFile", "/Volumes/Elements/github_archive/github-2019-06-01/commits.csv");
        config.put("spark.local.dir", "/Volumes/Elements/github_archive/spark_temp");
        config.put("spark.sql.warehouse.dir", "/Volumes/Elements/github_archive/spark_temp/warehouse");
        config.put("output.dir", "/Users/martinsteinhauer/Desktop/commitResult");
        config.put("master", "local[4]");
        DatasetExtractionPipeline pipeline = new DatasetExtractionPipeline(config);
        pipeline.execute();
        pipeline.shutdown();
    }

    static void executeRefactoringCommitPipeline(JobArgs jobArgs) {
        Map<String, Object> config = new HashMap<>();
        config.put("output.dir", jobArgs.getOutputDir().toString());
        config.put("topRepositoriesList", jobArgs.getInputFile().toString());
        config.put("jobs.parallel", jobArgs.getParallelJobs());
        config.put("github.user", jobArgs.getUsername());
        config.put("github.token", jobArgs.getToken());
        if(jobArgs.getDeployMode() == JobArgs.DeployMode.LOCAL) {
            config.put("spark.local.dir", "/Volumes/Elements/github_archive/spark_temp");
            config.put("spark.sql.warehouse.dir", "/Volumes/Elements/github_archive/spark_temp/warehouse");
        }

        RefactoringMiningPipeline pipeline = new RefactoringMiningPipeline(config);
        pipeline.execute();
        pipeline.shutdown();
    }
}
