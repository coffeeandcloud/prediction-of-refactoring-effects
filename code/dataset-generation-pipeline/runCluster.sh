#!/usr/bin/env bash

spark-submit --master "local[*]" \
--class it.unisa.softwaredependability.Main \
target/dataset-generation-pipeline-1.0-SNAPSHOT.jar \
--deploy-mode "CLUSTER" \
--input "s3n://your-bucket/top-15.csv" \
--output "s3n://your-bucket/commits" \
--parallel-jobs 3 \
--username <yourGithubUsername> \
--token <yourGithubAccessToken>
