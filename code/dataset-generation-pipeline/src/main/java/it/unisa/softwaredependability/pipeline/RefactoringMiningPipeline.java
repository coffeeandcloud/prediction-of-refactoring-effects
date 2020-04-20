package it.unisa.softwaredependability.pipeline;

import it.unisa.softwaredependability.config.DatasetHeader;
import it.unisa.softwaredependability.processor.RefactoringMiner;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

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

        repoList
                .map(row -> {
                    return RefactoringMiner.getInstance().execute(row.getString(0));
                })
                .first();
                // aggregate here the refactorings for each repository (cloning, mining...) and then
                // calculate the product metrics
    }

    @Override
    public void shutdown() {
        sparkSession.close();
    }
}
