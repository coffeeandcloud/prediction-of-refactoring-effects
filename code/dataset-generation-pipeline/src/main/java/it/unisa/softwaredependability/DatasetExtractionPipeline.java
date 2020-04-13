package it.unisa.softwaredependability;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


public class DatasetExtractionPipeline {

    public final String APP_NAME = "GithubDatasetExtraction";

    private static final Pattern SPACE = Pattern.compile(" ");


    private SparkConf sparkConf;

    public DatasetExtractionPipeline(String sourceFile) {
        sparkConf = new SparkConf().setAppName(APP_NAME);

        System.out.println("Starting app '" + APP_NAME + "'");

        JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        JavaRDD<String> lines = ctx.textFile(sourceFile, 1);


        JavaRDD<String> words = lines.flatMap(s -> Arrays.asList(SPACE.split(s)).iterator());
        JavaPairRDD<String, Integer> ones = words.mapToPair(word -> new Tuple2<>(word, 1));
        JavaPairRDD<String, Integer> counts = ones.reduceByKey((Integer i1, Integer i2) -> i1 + i2);

        List<Tuple2<String, Integer>> output = counts.collect();
        for (Tuple2<?, ?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }
        ctx.stop();
    }
}
