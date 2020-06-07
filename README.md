# Prediction of refactoring effects

## Introduction
This repository is the result of the [Software Dependability course](https://github.com/fpalomba/SWDependability-Unisa2020) at the university of Salerno. 
The goal was to predict the refactoring effects in open source software projects using machine learning techniques and therefore building a large scale 
dataset out of existing and large github repositories. It contains a pipeline that detects refactoring operations within
the given github repositories ([RefactoringMiner](https://github.com/tsantalis/RefactoringMiner)) and calculates some static code metrics before and after 
the refactoring operation [CK](https://github.com/mauricioaniche/ck). Big data tools like Apache Spark are used to enable distributed repository mining.

## Usage
The application is designed to be easily executed on several platforms. As every Spark job, it can run in local mode and in
a cluster with several worker nodes or on cloud platforms like Amazon EMR (Elastic MapReduce). 
