package it.unisa.softwaredependability.pipeline;

import it.unisa.softwaredependability.config.DatasetHeader;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Map;

public class DatasetExtractionPipeline extends Pipeline{

    private final String APP_NAME = "GithubDatasetExtraction";

    private SparkSession sparkSession;

    public DatasetExtractionPipeline(Map<String, Object> config) {
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
    public void execute() throws Exception {
        Dataset<Row> projectDataset = sparkSession.read()
                .format("csv")
                .option("header", "false")
                .option("mode", "DROPMALFORMED")
                .schema(DatasetHeader.getProjectHeader())
                .load((String)config.get("projectSourceFile"))
                .filter("language = 'Java'");

        Dataset<Row> commitDataset = sparkSession.read()
                .format("csv")
                .option("header", false)
                .option("mode", "DROPMALFORMED")
                .schema(DatasetHeader.getCommitHeader())
                .load((String)config.get("commitSourceFile"));

        JavaRDD<Row> results = projectDataset
                .join(commitDataset, projectDataset.col("id").equalTo(commitDataset.col("project_id")))
                .select("url")
                .groupBy("url")
                .count()
                .toJavaRDD();

        results.saveAsTextFile((String)config.get("output.dir"));
    }

    @Override
    public void shutdown() {
        sparkSession.close();
    }
}
