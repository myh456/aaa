package org.example.inspection.service.impl;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.Getter;
import org.example.inspection.service.DatabaseInspectionService;
import org.example.inspection.utils.EncryptionUtil;
import org.example.inspection.utils.InspectionEntityUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;

/**
 * @author myh
 */
@Service
public class DatabaseInspectionServiceImpl implements DatabaseInspectionService {
    @Resource
    private InspectionEntityUtil inspectionEntityUtil;
    @Resource
    private EncryptionUtil encryptionUtil;

    private final Map<String, List<Object>> results;

    public DatabaseInspectionServiceImpl() {
        results = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public void inspectionSingle(Map<String, Object> database, String checker, String time, String date) {
        // 获取基础属性
        Map<String, Object> attrs = new HashMap<>();
        for(String key: database.keySet()) {
            if(key.startsWith("@")) {
                attrs.put(key.substring(1), database.get(key));
            }
        }
        attrs.put("checker", checker);
        attrs.put("time", time);
        attrs.put("date", date);
        // TODO: 密码解密
        String url;
        String username = database.get("@username").toString();
        String password = encryptionUtil.decrypt(database.get("@password").toString());
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
        Db db = null;
        try {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            db = Db.use(dataSource);
        } catch (Exception e) {
            System.err.println("数据库连接失败" + database.get("name"));
        }
        for (Map.Entry<String, Object> entry : database.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.startsWith("@")) {
                continue;
            }
            if(value instanceof Map && ((Map<String, Object>) value).containsKey("sql")) {
                if (!results.containsKey(key)) {
                    results.put(key, new ArrayList<>());
                }
                try {
                    List<String> sqlList;
                    if(((Map<String, Object>) value).get("sql") instanceof String) {
                        sqlList = Collections.singletonList(((Map<String, Object>) value).get("sql").toString());
                    } else {
                        sqlList = (List<String>) ((Map<String, Object>) value).get("sql");
                    }
                    for(String sql: sqlList) {
                        List<Entity> entities;
                        entities = db.query(sql);
                        entities.forEach(entity -> {
                            entity.putAll(attrs);
                            if (((Map<String, Object>) value).containsKey("threshold")) {
                                Map<String, Object> threshold = new HashMap<>();
                                ((Map<String, Object>) ((Map<String, Object>) database.get(key)).get("threshold")).forEach((k1, v1) -> {
                                    Map<String, Object> item = new HashMap<>();
                                    ((Map<String, Object>) v1).forEach((k2, v2) -> item.put(k2.substring(1), v2));
                                    threshold.put(k1, item);
                                });
                                entity.put("threshold", threshold);
                            }
                        });
                        results.get(key).addAll(entities);
                    }
                } catch (Exception e) {
                    results.get(key).add(attrs);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, List<Object>> inspection(String checker, String time, String date) {
        List<Object> databases = inspectionEntityUtil.getDatabases();
        for (Object database : databases) {
            inspectionSingle((Map<String, Object>) database, checker, time, date);
        }
        return results;
    }
}
