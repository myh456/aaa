package org.example.inspection.service.impl;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.Getter;
import org.example.inspection.service.DatabaseInspectionService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;

@Getter
@Service
public class DatabaseInspectionServiceImpl implements DatabaseInspectionService {
    private final Map<String, List<Object>> results;

    public DatabaseInspectionServiceImpl() {
        results = new HashMap<>();
    }

    @Override
    public void inspection(Map<String, Object> database, String checker, String time, String date) throws SQLException {
        // TODO: 密码解密
        String url = "";
        String username = database.get("@username").toString();
        String password = database.get("@password").toString();
        switch (database.get("@type").toString()) {
            case "mysql5":
            case "mysql":
            case "mysql8":
                url = "jdbc:mysql://" + database.get("@ip") + ":" + database.get("@port");
                break;
            case "oracle":
                url = "jdbc:oracle:thin:@" + database.get("@ip") + ":" + database.get("@port");
                break;
            case "clickhouse":
                url = "jdbc:clickhouse://" + database.get("@ip") + ":" + database.get("@port");
                break;
            case "vertica":
                url = "jdbc:vertica://" + database.get("@ip") + ":" + database.get("@port");
                break;
            default:
                throw new RuntimeException("不支持的数据库类型");
        }
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        Db db = Db.use(dataSource);
        Map<String, Object> attrs = new HashMap<>();
        for(String key: database.keySet()) {
            if(key.startsWith("@"))
                attrs.put(key.substring(1), database.get(key));
        }
        database.forEach((key, value) -> {
            if (key.startsWith("@")) return;
            if(value instanceof Map && ((Map<String, Object>) value).containsKey("sql")) {
                List<String> sqlList;
                if(((Map<String, Object>) value).get("sql") instanceof String) {
                    sqlList = Collections.singletonList(((Map<String, Object>) value).get("sql").toString());
                } else {
                    sqlList = (List<String>) ((Map<String, Object>) value).get("sql");
                }
                for(String sql: sqlList) {
                    List<Entity> entities;
                    try {
                        entities = db.query(sql);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    entities.forEach(entity -> {
                        entity.putAll(attrs);
                        if(((Map<String, Object>) value).containsKey("threshold"))
                            entity.put("threshold", ((Map<String, Object>) value).get("threshold"));
                    });
                    if(!results.containsKey(key)) results.put(key, new ArrayList<>());
                    results.get(key).addAll(entities);
                }
            }
        });
    }
}
