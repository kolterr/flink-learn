package com.akazone.sink;


import com.alibaba.fastjson.JSONObject;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class MysqlSink extends RichSinkFunction<String> {
    private PreparedStatement state;
    private Connection conn;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        conn = getConnection();
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (conn != null) {
            conn.close();
        }
        if (state != null) {
            state.close();
        }
    }

    @Override
    public void invoke(String op, Context context) throws Exception {
        JSONObject operation = JSONObject.parseObject(op);
        switch (operation.getString("type")) {
            case "read":
            case "create":
            case "update":
                doInsertOrUpdate(operation);
                break;
            case "delete":
                doDelete(operation);
        }
    }

    private void doDelete(JSONObject op) throws Exception {
        String sql = "Delete From students_copy where id = ?";
        state = conn.prepareStatement(sql);
        JSONObject before = op.getJSONObject("beforeData");
        if (before != null) {
            state.setLong(1, before.getInteger("id"));
            state.executeUpdate();
            System.out.println(state);
        } else {
            System.out.println();
        }
    }

    private void doInsertOrUpdate(JSONObject op) throws Exception {
        String sql = "Insert into students_copy(id,name,gender,grade,score,last_login_at) Values (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name),gender = VALUES(gender),grade = VALUES(grade),score= VALUES (score),last_login_at=VALUES (last_login_at)";
        state = conn.prepareStatement(sql);
        JSONObject after = op.getJSONObject("afterData");
        if (after != null) {
            state.setLong(1, after.getInteger("id"));
            state.setString(2, after.getString("name"));
            state.setInt(3, after.getInteger("gender"));
            state.setInt(4, after.getInteger("grade"));
            state.setInt(5, after.getInteger("score"));
            Timestamp ts = new Timestamp(after.getLong("last_login_at"));
            state.setTimestamp(6, ts);
            System.out.println(state);
            state.executeUpdate();
        }
    }

    private static Connection getConnection() {
        Connection conn = null;
        try {
            String jdbc = "com.mysql.jdbc.Driver";
            Class.forName(jdbc);
            String url = "jdbc:mysql://localhost:3307/learnjdbc";
            String user = "root";
            String password = "123456";
            conn = DriverManager.getConnection(url + "?useUnicode=true"
                    + "&useSSL=false" + "&characterEncoding=UTF-8" +
                    "&serverTimezone=UTC", user, password);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}

