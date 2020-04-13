package it.unisa.softwaredependability;

import org.apache.spark.SparkConf;

public class Main {

    public static void main(String[] args) {
        for(String arg : args) {
            System.out.println(arg);
        }
        String sourceFile = "/Volumes/Elements/github archive/samples/spring-projects.csv";
        sourceFile = "/Volumes/Elements/github\\ archive/github-2019-06-01/projects.csv";
        DatasetExtractionPipeline pipeline = new DatasetExtractionPipeline(sourceFile);
    }
}
