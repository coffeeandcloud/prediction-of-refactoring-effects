package it.unisa.softwaredependability.pipeline;

import com.google.common.collect.Lists;
import it.unisa.softwaredependability.config.DatasetHeader;
import it.unisa.softwaredependability.model.metrics.MetricResult;
import it.unisa.softwaredependability.processor.CommitSplitter;
import it.unisa.softwaredependability.processor.DiffContentExtractor;
import it.unisa.softwaredependability.processor.RepositoryResolver;
import it.unisa.softwaredependability.processor.StaticRefactoringMiner;
import it.unisa.softwaredependability.processor.metric.CKMetricProcessor;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RefactoringMiningPipeline extends Pipeline  {

    private SparkSession sparkSession;

    private final String APP_NAME = "RefactoringMiningPipeline";

    private final static transient Logger log = Logger.getLogger("RefactoringMiningPipeline");

    public RefactoringMiningPipeline(Map<String, Object> config) {
        super(config);
    }

    @Override
    public void init(Map<String, Object> config) {
        sparkSession = SparkSession.builder()
                .appName(APP_NAME)
                .getOrCreate();
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
                .load((String) config.get("repository.list"))
                .toJavaRDD();

        JavaRDD<String> repos = repoList
                .repartition((Integer)config.get("repos.parallel"))
                .map(row -> resolver.resolveGithubApiUrl(row.getString(0)));

        JavaRDD<Row> commits = repos
                .flatMap(s -> new CommitSplitter((Integer) config.get("batch.size")).executeSingle(s).iterator())
                .repartition((Integer) config.get("jobs.parallel"))
                // row mapping is done within the StaticRefactoringMiner due to performance optimizations
                .flatMap(x -> StaticRefactoringMiner.executeBlockingList(x).iterator());

        sparkSession.createDataFrame(commits, DatasetHeader.getSmallRefactoringCommitHeader())
                .write()
                .parquet((String) config.get("output.dir.commits"));

        JavaRDD<Row> commitMetricResults = commits
                .repartition((Integer) config.get("jobs.parallel"))
                .mapPartitions(partitions -> {
                    List<List<Row>> partition = Lists.partition(Lists.newArrayList(partitions), 1000);
                    // Alternative: return partial list
                    List<MetricResult> metricResults = new ArrayList<>();
                    for(List<Row> p: partition) {
                        DiffContentExtractor extractor = new DiffContentExtractor()
                                .addMetricProcessor(new CKMetricProcessor());
                        metricResults.addAll(extractor.executeBatch(p));
                    }
                    return metricResults.iterator();
                }).flatMap(x -> x.toRow().iterator());

        sparkSession.createDataFrame(commitMetricResults, DatasetHeader.getCommitHeaderWithMetrics())
                .write()
                .parquet((String) config.get("output.dir.metrics"));
    }

    @Override
    public void shutdown() {
        try {
            sparkSession.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
