package it.unisa.softwaredependability.pipeline;

import it.unisa.softwaredependability.config.DatasetHeader;
import it.unisa.softwaredependability.model.GitRefactoringCommit;
import it.unisa.softwaredependability.processor.RefactoringMiner;
import it.unisa.softwaredependability.processor.RepositoryResolver;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.Map;

public class RefactoringMiningPipeline extends Pipeline{

    private SparkSession sparkSession;

    private final String APP_NAME = "RefactoringMiningPipeline";


    public RefactoringMiningPipeline(Map<String, Object> config) {
        super(config);
    }

    @Override
    public void init(Map<String, Object> config) {
        sparkSession = SparkSession.builder()
                .master((String)config.get("master"))
                .appName(APP_NAME)
                .config("spark.local.dir", (String)config.get("spark.local.dir"))
                .config("spark.sql.warehouse.dir", (String)config.get("spark.sql.warehouse.dir"))
                .getOrCreate();

        System.out.println("Starting app '" + APP_NAME + "'");
    }

    @Override
    public void execute() {
        JavaRDD<Row> repoList = sparkSession.read()
                .format("csv")
                .option("header", "false")
                .option("mode", "DROPMALFORMED")
                .schema(DatasetHeader.getCommitCountHeader())
                .load((String) config.get("topRepositoriesList"))
                .toJavaRDD();

        long count = repoList
                .map(row -> RepositoryResolver.resolveGithubApiUrl(row.getString(0)))
                .map(url -> RefactoringMiner.getInstance().execute(url))
                .flatMap((List<GitRefactoringCommit> list) -> {
                    return list.iterator();
                })
                .map(refactoring -> {
                    return refactoring
                })
                .saveAsTextFile("");

        System.out.println("Found commits: " + count);
                // aggregate here the refactorings for each repository (cloning, mining...) and then
                // calculate the product metrics
    }

    @Override
    public void shutdown() {
        sparkSession.close();
    }
}
