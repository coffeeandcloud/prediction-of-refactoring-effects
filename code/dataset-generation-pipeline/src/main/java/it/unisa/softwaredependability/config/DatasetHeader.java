package it.unisa.softwaredependability.config;

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.IntegerType;
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
                .add("created_at", DataTypes.TimestampType, true)
                .add("forked_from", DataTypes.IntegerType, true)
                .add("deleted", DataTypes.IntegerType, true)
                .add("updated_at", DataTypes.TimestampType, true);
    }
}
