package it.unisa.softwaredependability.config;

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class DatasetHeader {

    public static StructType getProjectHeader() {
        return new StructType()
                .add("id", DataTypes.IntegerType, true)
                .add("url", DataTypes.StringType, true)
                .add("owner_id", DataTypes.IntegerType, true)
                .add("name", DataTypes.StringType, true)
                .add("description", DataTypes.StringType, true)
                .add("language", DataTypes.StringType, true)
                .add("project_created_at", DataTypes.TimestampType, true)
                .add("forked_from", DataTypes.IntegerType, true)
                .add("deleted", DataTypes.IntegerType, true)
                .add("updated_at", DataTypes.TimestampType, true);
    }

    public static StructType getCommitHeader() {
        return new StructType()
                .add("commit_id", DataTypes.IntegerType, true)
                .add("sha", DataTypes.StringType, true)
                .add("author_id", DataTypes.IntegerType, true)
                .add("committer_id", DataTypes.IntegerType, true)
                .add("project_id", DataTypes.IntegerType, true)
                .add("commit_created_at", DataTypes.TimestampType, true);
    }

    public static StructType getCommitCountHeader() {
        return new StructType()
                .add("url", DataTypes.StringType, false)
                .add("count", DataTypes.IntegerType, false);
    }
}
