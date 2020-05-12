package it.unisa.softwaredependability.pipeline;


import org.apache.spark.SparkConf;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.util.Map;

public class StreamingPipeline extends Pipeline{

    private JavaStreamingContext streamingContext;

    public StreamingPipeline(Map<String, Object> config) {
        super(config);
    }

    @Override
    public void init(Map<String, Object> config) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("Streaming Refactoring");
        streamingContext = new JavaStreamingContext(conf, Durations.seconds(1));
    }

    @Override
    public void execute() throws Exception {
        JavaReceiverInputDStream<String> input = streamingContext.socketTextStream("localhost", 9999, StorageLevel.MEMORY_AND_DISK());



        streamingContext.start();              // Start the computation
        streamingContext.awaitTermination();   // Wait for the computation to terminate
    }

    @Override
    public void shutdown() {

    }
}
