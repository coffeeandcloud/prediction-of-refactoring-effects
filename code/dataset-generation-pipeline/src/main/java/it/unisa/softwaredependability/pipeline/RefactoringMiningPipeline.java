package it.unisa.softwaredependability.pipeline;

import it.unisa.softwaredependability.config.DatasetHeader;
import it.unisa.softwaredependability.processor.RefactoringMiner;
import it.unisa.softwaredependability.processor.RefactoringMinerIterator;
import it.unisa.softwaredependability.processor.RepositoryResolver;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.StreamingContext;

import java.util.Map;

public class RefactoringMiningPipeline extends Pipeline  {

    private SparkSession sparkSession;

    private StreamingContext jssc;

    private final String APP_NAME = "RefactoringMiningPipeline";

    public RefactoringMiningPipeline(Map<String, Object> config) {
        super(config);
    }

    @Override
    public void init(Map<String, Object> config) {
        sparkSession = SparkSession.builder()
                .appName(APP_NAME)
                //.config("spark.local.dir", (String)config.get("spark.local.dir"))
                //.config("spark.sql.warehouse.dir", (String)config.get("spark.sql.warehouse.dir"))
                .getOrCreate();

        SparkConf conf = new SparkConf();

        System.out.println("Starting app '" + APP_NAME + "'");
    }

    @Override
    public void execute() {
        RepositoryResolver resolver = RepositoryResolver
                .getInstance((String) config.get("github.user"), (String) config.get("github.token"));

        JavaRDD<Row> repoList = sparkSession.read()
                .format("csv")
                .option("header", "false")
                .option("mode", "DROPMALFORMED")
                .schema(DatasetHeader.getCommitCountHeader())
                .load((String) config.get("topRepositoriesList"))
                .toJavaRDD();

        JavaRDD<String> repos = repoList
                .repartition((Integer)config.get("jobs.parallel"))
                .map(row -> resolver.resolveGithubApiUrl(row.getString(0)));

        JavaRDD<Row> map = repos
                .flatMap(RefactoringMinerIterator::new);

        sparkSession.createDataFrame(map, DatasetHeader.getRefactoringCommitHeader())
                .write()
                .parquet((String) config.get("output.dir"));

        RefactoringMiner.getInstance().cleanup();


    }

    @Override
    public void shutdown() {
        sparkSession.close();
    }
}
