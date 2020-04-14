package it.unisa.softwaredependability;

import it.unisa.softwaredependability.config.DatasetHeader;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;


public class DatasetExtractionPipeline {

    public final String APP_NAME = "GithubDatasetExtraction";

    public DatasetExtractionPipeline(String sourceFile) {

        SparkSession session = SparkSession.builder()
                .master("local[8]")
                .appName(APP_NAME)
                .getOrCreate();

        System.out.println("Starting app '" + APP_NAME + "'");


        Dataset<Row> dataset = session.read()
                .format("csv")
                .option("header", "false")
                .option("mode", "DROPMALFORMED")
                .schema(DatasetHeader.getProjectHeader())
                .load(sourceFile);

        JavaRDD<Row> results = dataset
                .filter("id != -1")
                .select("language")
                .groupBy("language")
                .count()
                .toJavaRDD();

        results.saveAsTextFile("/Users/martinsteinhauer/Desktop/results.txt");

        session.close();
    }
}
