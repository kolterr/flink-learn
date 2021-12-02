package com.akazone.stream;

import com.akazone.model.MysqlDeserializationSchema;
import com.akazone.sink.MysqlSink;
import com.ververica.cdc.connectors.mysql.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.DebeziumSourceFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class mysql2mysql {
    public static void main(String[] args) throws Exception {
        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        env.setStateBackend(new EmbeddedRocksDBStateBackend());
        checkpointConfig.setCheckpointStorage(parameterTool.get("checkpoint_dir"));
        // 任务流取消和故障时会保留Checkpoint数据，以便根据实际需要恢复到指定的Checkpoint
        checkpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // 设置checkpoint的周期, 每隔30000 ms进行启动一个检查点
        checkpointConfig.setCheckpointInterval(30000);
        // 设置模式为exactly-once
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        // 确保检查点之间有至少500 ms的间隔【checkpoint最小间隔】
        checkpointConfig.setMinPauseBetweenCheckpoints(500);
        // 检查点必须在一分钟内完成，或者被丢弃【checkpoint的超时时间】
        checkpointConfig.setCheckpointTimeout(60000);
        // 同一时间只允许进行一个检查点
        checkpointConfig.setMaxConcurrentCheckpoints(1);
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(0, 10L));
        DataStreamSource<String> src = env.addSource(getSourceFunction(parameterTool));
        src.print().setParallelism(1);
        src.addSink(new MysqlSink());
        env.execute(parameterTool.get("app_name"));
    }

    // 获取source
    public static DebeziumSourceFunction<String> getSourceFunction(ParameterTool tool) {
        DebeziumSourceFunction<String> mySqlSource = MySqlSource.<String>builder()
                .hostname(tool.get("db_host"))
                .port(Integer.parseInt(tool.get("db_port")))
                .databaseList(tool.get("db_databases")) // set captured database
                .tableList(tool.get("db_table_list"))
                .username(tool.get("db_user"))
                .password(tool.get("db_user_pass"))
                .deserializer(new MysqlDeserializationSchema())
                .startupOptions(StartupOptions.initial())
                .build();
        return mySqlSource;
    }
}
