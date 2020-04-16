package it.unisa.softwaredependability;

import it.unisa.softwaredependability.config.DatasetHeader;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import org.apache.spark.sql.functions.*;

public class DatasetExtractionPipeline {

    public final String APP_NAME = "GithubDatasetExtraction";

    public DatasetExtractionPipeline(String projectSourceFile, String commitSourceFile) {

        SparkSession session = SparkSession.builder()
                .master("local[8]")
                .appName(APP_NAME)
                .config("spark.local.dir", "/Volumes/Elements/github_archive/spark_temp")
                .config("spark.sql.warehouse.dir", "/Volumes/Elements/github_archive/spark_temp/warehouse")
                .getOrCreate();

        System.out.println("Starting app '" + APP_NAME + "'");


        Dataset<Row> projectDataset = session.read()
                .format("csv")
                .option("header", "false")
                .option("mode", "DROPMALFORMED")
                .schema(DatasetHeader.getProjectHeader())
                .load(projectSourceFile)
                .filter("language = 'Java'");

        Dataset<Row> commitDataset = session.read()
                .format("csv")
                .option("header", false)
                .option("mode", "DROPMALFORMED")
                .schema(DatasetHeader.getCommitHeader())
                .load(commitSourceFile);

        JavaRDD<Row> results = projectDataset
                .join(commitDataset, projectDataset.col("id").equalTo(commitDataset.col("project_id")))
                .select("url")
                .groupBy("url")
                .count()
                .toJavaRDD();

        results.saveAsTextFile("/Users/martinsteinhauer/Desktop/commitResults");

        session.close();
    }
}
