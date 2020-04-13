package it.unisa.softwaredependability;

import org.apache.spark.SparkConf;

public class Main {

    public static void main(String[] args) {
        DatasetExtractionPipeline pipeline = new DatasetExtractionPipeline("/Volumes/Elements/github\\ archive/github-2019-06-01/projects.csv");
    }
}
