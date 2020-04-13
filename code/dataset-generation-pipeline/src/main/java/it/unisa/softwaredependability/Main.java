package it.unisa.softwaredependability;

import org.apache.spark.SparkConf;

public class Main {

    public static void main(String[] args) {
        DatasetExtractionPipeline pipeline = new DatasetExtractionPipeline("/Users/martinsteinhauer/sdk/hadoop/LICENSE.txt");
    }
}
