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

    public static StructType getRefactoringCommitHeader() {
        StructType refactoring = new StructType()
                .add("file", DataTypes.StringType)
                .add("startLine", DataTypes.IntegerType)
                .add("endLine", DataTypes.IntegerType)
                .add("startColumn", DataTypes.IntegerType)
                .add("endColumn", DataTypes.IntegerType)
                .add("codeElementType", DataTypes.StringType)
                .add("description", DataTypes.StringType)
                .add("codeElement", DataTypes.StringType);

        return new StructType()
                .add("repository", DataTypes.StringType)
                .add("commit_id", DataTypes.StringType)
                .add("type", DataTypes.StringType)
                .add("description", DataTypes.StringType)
                .add("before", DataTypes.createArrayType(refactoring))
                .add("after", DataTypes.createArrayType(refactoring));
    }

    public static StructType getSmallRefactoringCommitHeader() {
        return new StructType()
                .add("repository", DataTypes.StringType)
                .add("commit_id", DataTypes.StringType)
                .add("type", DataTypes.StringType);
    }
}
