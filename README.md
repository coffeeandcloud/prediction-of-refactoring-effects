# Prediction of refactoring effects

## Introduction
This repository is the result of the [Software Dependability course](https://github.com/fpalomba/SWDependability-Unisa2020) at the university of Salerno. 
The goal was to predict the refactoring effects in open source software projects using machine learning techniques and therefore building a large scale 
dataset out of existing and large github repositories. It contains a pipeline that detects refactoring operations within
the given github repositories ([RefactoringMiner](https://github.com/tsantalis/RefactoringMiner)) and calculates some static code metrics before and after 
the refactoring operation [CK](https://github.com/mauricioaniche/ck). Big data tools like Apache Spark are used to enable distributed repository mining.

## Usage
The application is designed to be easily executed on several platforms. As every Spark job, it can run in local mode and in
a cluster with several worker nodes, on cloud platforms like Amazon EMR (Elastic MapReduce) or even with multiple executors 
running on one machine.

### Building the project
Spark only works with Java JDK 8 or OpenJDK 8. Also maven is required to build this project. Execute `mvn clean package` to build the jar
file which Spark will execute. The POM is configured to include all needed dependencies within this jar. Also shaded packaging is enabled
to avoid conflicts between different library versions (e.g. multiple conflicting versions of Google Guava).

Additionally, you need to build the adapted version of RefactoringMiner which can be found as a forked project [here](https://github.com/im-a-giraffe/RefactoringMiner). After cloning, just run `gradle install` to build the jars and install them to your local repository. It is important to build this project with JDK 8, because otherwise you will run into problems when including it in the Spark application which is only able to run on version <= Java 8.

### Spark framework setup
Make sure that you first download the [Apache Spark binary](https://spark.apache.org/downloads.html) (version 2.4.5)
with Hadoop binaries included. Just unpack the archive and set the environment variable `SPARK_HOME` to that directory. Additonally,
you can add the `SPARK_HOME/bin` path to your path variable for simpler execution. Spark on Winows needs some additional 
setup, please refer to manuals which describe the setup process for windows/mac/linux in more detail.

### Local mode
The local mode is the easiest way to try the Spark application. Have a look at the `runLocal.sh` script to get basic understanding
how a Spark job can be submitted. The application is parametrized and contains the following configuration parameter:

**--master "local[1]":** Defines the mode how Spark is executed. Local means only one driver and executor are started on the local computer.

**--class it.unisa.softwaredependability.Main:** Main class which Spark will execute

**--driver-memory 8g:** Limit how much memory the driver is allowed to use. Driver memory is mainly important for local mode since there driver and executor is combined. 8gb is good value for run this application in single thread mode.

**--conf "spark.serializer=org.apache.spark.serializer.KryoSerializer":** Objects between jobs are serialized when written to disk. Cryo is faster than the default Java serialization functionality.

**target/dataset-generation-pipeline-1.0-SNAPSHOT.jar:** Path to the built jar file

**--input "datasets/small.csv":** CSV file which contains the API path to the github repository and a number of commit counts. Examples can be found in `/datasets` folder or genereted with the DatasetExtactionPipeline contained within this project (extraction from GHTorrent files, not accessable from CLI, need to call the right pipeline in code).

**--commits-output "commits":** Directory where Parquet files are written to (intermediate results, list of commits containing refactorings)

**--metrics-output "commitmetricresults":**  Directory where the results are written to. Both paths can be also be on HDFS compatible file systems (including AWS S3 storage, just start the path with `s3n://mybucket/folder)
**--parallel-jobs 1:** Number of repositories that are mined in parallel
**--parallel-repos 1:** Number of parallel jobs the mining process is split into. This value should match the number of executors within the cluster for best performance.
- **--batch-size 100:** Size of the generated batches the commits and files are processed. Higher batch size increases the computation speed due to less overhead, but can exceed the heap space when chosen to large. Values between 100 and 1000 are a good start, depending on the size of the repository and amout of memory installed on the machines.

**--username "------------":** Your github username to resolve the api format of the repositories extracted from GHTorrent

**--token "------------"** Token for accessing the github api, can be generated in your Github settings.

### Cluster mode
Cluster mode is the more important mode to run Spark. You can submit to a running cluster by using the script `runLocalCluster.sh` which defers
only in the following two parameters:
- **--master "spark://myclusteradress:7077":** The address of the spark master node the job is submitted to.
- **--executor-memory: 8g:** Defines how much memory one executor can use to execute the job. 8gb per executor is good value for larger repositories.


## Architecture
The mining application is built modular to allow a simple extension. The following classes are important for adapting the application.

### Pipeline
This class is designed abstract and allows the implementation of an additional pipeline step. Methods for cleaning up the directories
after running the job are implemented and can be modified.
- `DatasetExtractionPipeline`: This pipeline allows the extraction of the repositories with the most commits from the GHTorrent archive and requires the CSV files from their website (the MySQL dump).
- `RefactoringMiningPipeline`: The heart of this project. Allows the extraction of refactoring commits in the first part and, with an intermediate result, afterwards calculate the metrics. Both parts are heavily built on JGit and allow a simple reconfiguration to filter for other commits one is interested in. It contains the following steps (listed chronological):
-- `RepositoryResolver`: Checks if the repository is still available by calling the github api and get's the full url back.
-- `CommitSplitter`: Splits the commit by the given batch size to avoid out of memory errors in larger repositories and enable the parallelization.
-- `StaticRefactoringMiner`: Wraps the RefactoringMiner library and outputs a list of refactoring operations. Please note that this project uses an slighty adapted version of RepositoryMiner which can be found [here](https://github.com/im-a-giraffe/RefactoringMiner). Just use clone the repo and use `mvn clean install` enable the dependency resolving. 
-- `DiffContentExtractor`: This calculates the diff between the earlier extracted refactoring commit id and its parent. Also checks out the repository by this commit id to make the correct files for CK library available.

### Metric Processor
This interface builds the abstraction of a metric calculation unit and can be implemented with other metric generation libraries as well. `CKMetricProcessor` is an implementation
that make use of the CK library for static code metric generation. `DiffContentExtractor` takes a list of `MetricProcessor` to enable the computation of multiple (different) metrics.

As a result, the `MetricProcessor` returns the templated data type `Metric<T>` which enables the generation of different outputs (this may require some adaptions in the parquet writing, see `config.DatasetHeader` for different types of headers (schemas) defined for the parquet files). 
The list of generated metrics (which could be more than one per commit or operation), are wrapped in the class `MetricResult` which holds a list of metric objects. MetricResult gets flattened when
stored as a parquet file, meaning every row contains the basis information about the commit and refactoring, and for each row different `Metric<T>` data.
