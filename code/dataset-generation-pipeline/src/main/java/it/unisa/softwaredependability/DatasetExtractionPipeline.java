package it.unisa.softwaredependability;

import it.unisa.softwaredependability.config.DatasetHeader;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


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
                .load(sourceFile)
                .select("*")
                .where("language = 'Java'");

        System.out.println("Found projects in Java: "+dataset.count());



        session.close();
    }
}
