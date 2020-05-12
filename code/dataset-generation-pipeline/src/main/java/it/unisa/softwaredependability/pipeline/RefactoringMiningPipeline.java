package it.unisa.softwaredependability.pipeline;

import it.unisa.softwaredependability.config.DatasetHeader;
import it.unisa.softwaredependability.processor.CommitSplitter;
import it.unisa.softwaredependability.processor.RefactoringMinerIterator;
import it.unisa.softwaredependability.processor.RepositoryResolver;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RefactoringMiningPipeline extends Pipeline  {

    private SparkSession sparkSession;

    private final String APP_NAME = "RefactoringMiningPipeline";

    private transient Logger log = Logger.getLogger(getClass().getName());

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

        //log.info("Starting app '" + APP_NAME + "'");
    }

    @Override
    public void execute() throws Exception {
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

        JavaRDD<Row> commits = repos
                .flatMap(s -> new CommitSplitter().execute(s).iterator())
                .flatMap(range -> {
                    List<Row> rows = RefactoringMinerIterator.executeBlocking(range);
                    return rows.iterator();
                });



        /*
        sparkSession.createDataFrame(repoCommitRangeJavaRDD, new StructType().
                add("startCommit", DataTypes.StringType).add("endCommit", DataTypes.StringType).add("repo", DataTypes.StringType))
                .groupBy("repo")
                .count()
                .show();



        sparkSession.createDataFrame(repoCommits, new StructType().add("hash", DataTypes.StringType))
                .write()
                .parquet((String) config.get("output.dir"));


        JavaRDD<Row> map = repos
                .flatMap(repoUrl -> new RefactoringMinerIterator(repoUrl, (String) config.get("github.branch")));


         */


        sparkSession.createDataFrame(commits, DatasetHeader.getSmallRefactoringCommitHeader())
                .write()
                .parquet((String) config.get("output.dir"));



    }

    @Override
    public void shutdown() {
        try {
            RefactoringMinerIterator.cleanupTempFiles();
            sparkSession.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
