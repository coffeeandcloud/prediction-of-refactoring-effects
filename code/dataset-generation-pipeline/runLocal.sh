#!/usr/bin/env bash

rm -R /Users/martinsteinhauer/Desktop/commits
rm -R /Users/martinsteinhauer/Desktop/commitmetricresults

~/sdk/spark/bin/spark-submit --master "local[1]" \
--class it.unisa.softwaredependability.Main \
--driver-memory 8g \
--conf "spark.serializer=org.apache.spark.serializer.KryoSerializer" \
target/dataset-generation-pipeline-1.0-SNAPSHOT.jar \
--input "datasets/small.csv" \
--commits-output "path/to/dir/commits" \
--metrics-output "path/to/dir/commitmetricresults" \
--refactoring-mining-only \
--parallel-jobs 1 \
--parallel-repos 1 \
--batch-size 100 \
--username "<yourGithubUsername>" \
--token "<yourGithubAccessToken>"
