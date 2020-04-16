package it.unisa.softwaredependability;

import org.apache.spark.SparkConf;

public class Main {

    public static void main(String[] args) {
        for(String arg : args) {
            System.out.println(arg);
        }
        String projectSourceFile = "/Volumes/Elements/github_archive/github-2019-06-01/projects.csv";
        String commitSourceFile = "/Volumes/Elements/github_archive/github-2019-06-01/commits.csv";
        DatasetExtractionPipeline pipeline = new DatasetExtractionPipeline(projectSourceFile, commitSourceFile);
    }
}
