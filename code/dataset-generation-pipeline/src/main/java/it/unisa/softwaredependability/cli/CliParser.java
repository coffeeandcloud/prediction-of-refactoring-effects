package it.unisa.softwaredependability.cli;

import com.beust.jcommander.JCommander;

public class CliParser {

    public static JobArgs parse(String[] args) {
        JobArgs jobArgs = new JobArgs();
        JCommander.newBuilder()
                .addObject(jobArgs)
                .build()
                .parse(args);
        // TODO validate input data
        return jobArgs;
    }
}
