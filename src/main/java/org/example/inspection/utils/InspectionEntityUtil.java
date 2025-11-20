package org.example.inspection.utils;

import lombok.Getter;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 巡检配置工具类
 * 用于加工并获取和巡检相关所有配置
 */
@Component
@SuppressWarnings("unchecked")
public class InspectionEntityUtil {
    @Autowired
    private XmlParser xmlParser;
    @Value("${config.path}")
    private String configPath;

    @Getter
    private Map<String, Object> cmds;
    @Getter
    private Map<String, Object> sqls;
    @Getter
    private Map<String, Map<Integer, Object>> rules;
    @Getter
    private Map<Integer, Object> charas;
    @Getter
    private Map<String, Object> defaultConfig;
    @Getter
    private Map<String, Object> defaultCheck;
    @Getter
    private List<Object> servers;
    @Getter
    private List<Object> databases;

    @PostConstruct
    public void init() throws DocumentException {
        initConfig();
        initServer();
        initDatabase();
    }

    public void initConfig() throws DocumentException {
        // 初始化命令配置
        cmds = new HashMap<>();
        sqls = new HashMap<>();
        Map<String, Object> runs = xmlParser.parseXml(configPath + "system/ServersRunning.xml");
        runs.forEach((k, v) -> {
            if(v instanceof Map) {
                if (((Map<String, Object>) v).containsKey("cmd")) {
                    cmds.put(k, v);
                } else if (((Map<String, Object>) v).containsKey("sql")) {
                    sqls.put(k, v);
                }
            } else {
                if(((Map<String, Object>)((List<Object>) v).get(0)).containsKey("cmd")) {
                    cmds.put(k, v);
                } else if(((Map<String, Object>)((List<Object>) v).get(0)).containsKey("sql")) {
                    sqls.put(k, v);
                }
            }
        });
        // 初始化巡检规则配置
        Map<String, Object> rulesMap = xmlParser.parseXml(configPath + "RulesConfig.xml");
        rules = new HashMap<>();
        rulesMap.forEach((k, v) -> {
            rules.put(k, new HashMap<>());
            if(v instanceof List) {
                for(Object v1: (List<Object>) v) {
                    rules.get(k).put(Integer.parseInt(((Map<String, Object>) v1).get("@id").toString()), v1);
                }
            } else {
                rules.get(k).put(Integer.parseInt(((Map<String, Object>) v).get("@id").toString()), v);
            }
        });
        // 初始化角色配置
        Map<String, Object> charasMap = xmlParser.parseXml(configPath + "CharacterConfig.xml");
        charas = new HashMap<>();
        charasMap.forEach((k, v) -> {
            List<Object> charaList;
            if(v instanceof Map) {
                charaList = Collections.singletonList(v);
            } else {
                charaList = (List<Object>) v;
            }
            for(Object c: charaList) {
                Map<String, Object> chara = (Map<String, Object>) c;
                ((Map<String, Object>) c).forEach((k1, v1) -> {
                    if (k1.startsWith("@")) return;
                    if (v1 instanceof Map && ((Map<String, Object>) v1).containsKey("@id")) {
                        Integer id = Integer.parseInt(((Map<String, Object>) v1).get("@id").toString());
                        chara.put(k1, rules.get(k1).get(id));
                    }
                });
                charas.put(Integer.parseInt(chara.get("@id").toString()), chara);
            }
        });
        // 默认配置
        Map<String, Object> defaultMap = xmlParser.parseXml(configPath + "DefaultConfig.xml");
        defaultCheck = new HashMap<>();
        defaultConfig = defaultMap;
        Map<String, Object> checks = (Map<String, Object>) defaultMap.get("check");
        defaultConfig.remove("check");
        for (String k : checks.keySet()) {
            if (((Map<String, Object>) checks.get(k)).containsKey("@id")) {
                Map<String, Object> item = (Map<String, Object>) rules.get(k).get(Integer.parseInt(((Map<String, Object>) checks.get(k)).get("@id").toString()));
                defaultCheck.put(k, item);
            } else {
                defaultCheck.put(k, checks.get(k));
            }
        }
    }

    public void initServer() throws DocumentException {
        Map<String, Object> serversMap = xmlParser.parseXml(configPath + "ServersConfig.xml");
        servers = new ArrayList<>();
        List<Object> serverList;
        if(serversMap.get("server") instanceof Map) {
            serverList = Collections.singletonList(serversMap.get("server"));
        } else {
            serverList = (List<Object>)serversMap.get("server");
        }
        for(Object item: serverList) {
            Map<String, Object> check = (Map<String, Object>) item;
            Map<String, Object> server = new HashMap<>();
            boolean isDefault = true;
            for(String k : check.keySet()) {
                if(k.startsWith("@")) server.put(k, check.get(k));
                if(!k.equals("character")) continue;
                List<Object> charaList;
                if(check.get(k) instanceof Map) {
                    charaList = Collections.singletonList(check.get(k));
                } else {
                    charaList = (List<Object>) check.get(k);
                }
                for(Object ch: charaList) {
                    int id = Integer.parseInt(((Map<String, Object>) ch).get("@id").toString());
                    server.putAll((Map<String, Object>) charas.get(id));
                    isDefault = false;
                }
            }
            if(isDefault) {
                defaultCheck.forEach((k, v) -> {
                    if(((Map<String, Object>)v).containsKey("cmd"))
                        server.put(k, v);
                });
            }
            // 将命令插入服务器配置
            server.forEach((k, v) -> {
                if(k.startsWith("@")) return;
                if(cmds.containsKey(k)) {
                    // 保存服务器配置的变量
                    Map<String, List<String>> serverVars = new HashMap<>();
                    ((Map<String, Object>)v).forEach((k1, v1) -> {
                        if(k1.endsWith("-attr")) {
                            String attr = k1.substring(0, k1.length() - 5);
                            if(v1 instanceof List) serverVars.put(attr, (List<String>) v1);
                            else serverVars.put(attr, Collections.singletonList(v1.toString()));
                        }
                    });
                    // 获取并插入命令
                    Object cmd = cmds.get(k);
                    if(cmd instanceof Map) {
                        ((Map<String, Object>) v).putAll((Map<String, Object>)cmd);
                    } else {
                        for(Object c: (List<Object>)cmd) {
                            if(((Map<String, Object>) c).get("@os").equals(server.get("@os"))) {
                                ((Map<String, Object>) v).putAll((Map<String, Object>) c);
                                break;
                            }
                        }
                    }
                    // 根据变量生成cmd数组
                    if(!serverVars.isEmpty()) {
                        cmd = ((Map<String, Object>) v).get("cmd").toString();
                        List<String> cmds = new ArrayList<>();
                        for(String var: serverVars.keySet()) {
                            if(cmd.toString().contains(String.format("{{%s}}", var))) {
                                for(String val: serverVars.get(var)) {
                                    cmds.add(cmd.toString().replace(String.format("{{%s}}", var), val));
                                }
                            }
                        }
                        ((Map<String, Object>) v).put("cmd", cmds);
                    }
                }
            });
            servers.add(server);
        }
    }

    public void initDatabase() throws DocumentException {
        Map<String, Object> databaseMap = xmlParser.parseXml(configPath + "DatabaseConfig.xml");
        databases = new ArrayList<>();
        List<Object> databaseList;
        if(databaseMap.get("database") instanceof Map) {
            databaseList = Collections.singletonList(databaseMap.get("database"));
        } else {
            databaseList = (List<Object>)databaseMap.get("database");
        }
        for(Object item: databaseList) {
            Map<String, Object> check = (Map<String, Object>) item;
            Map<String, Object> database = new HashMap<>();
            boolean isDefault = true;
            for(String k : check.keySet()) {
                if(k.startsWith("@")) database.put(k, check.get(k));
                if(!k.equals("character")) continue;
                List<Object> charaList;
                if(check.get(k) instanceof Map) {
                    charaList = Collections.singletonList(check.get(k));
                } else {
                    charaList = (List<Object>) check.get(k);
                }
                for(Object ch: charaList) {
                    int id = Integer.parseInt(((Map<String, Object>) ch).get("@id").toString());
                    database.putAll((Map<String, Object>) charas.get(id));
                    isDefault = false;
                }
            }
            if(isDefault) {
                defaultCheck.forEach((k, v) -> {
                    if(((Map<String, Object>)v).containsKey("sql"))
                        database.put(k, v);
                });
            }
            // 将sql插入数据库配置
            Map<String, Object> databaseTemp = new HashMap<>(database);
            databaseTemp.forEach((k, v) -> {
                if(k.startsWith("@")) return;
                List<Object> sqlList;
                if(v instanceof Map) {
                    sqlList = Collections.singletonList(v);
                } else {
                    sqlList = new ArrayList<>((List<Object>) v);
                }
                for(Object sql: sqlList) {
                    if (sqls.containsKey(k)) {
                        // 保存数据库配置的变量
                        Map<String, List<String>> databaseVars = new HashMap<>();
                        ((Map<String, Object>) v).forEach((k1, v1) -> {
                            if (k1.endsWith("-attr")) {
                                String attr = k1.substring(0, k1.length() - 5);
                                if (v1 instanceof List) databaseVars.put(attr, (List<String>) v1);
                                else databaseVars.put(attr, Collections.singletonList(v1.toString()));
                            }
                        });
                        // 获取并插入sql
                        sql = sqls.get(k);
                        if (sql instanceof Map) {
                            ((Map<String, Object>) v).putAll((Map<String, Object>) sql);
                        } else {
                            for (Object s : (List<Object>) sql) {
                                if (((Map<String, Object>) s).get("@name").equals(database.get("@name"))) {
                                    ((Map<String, Object>) v).putAll((Map<String, Object>) s);
                                    break;
                                }
                            }
                        }
                        // 根据变量生成sql数组
                        if (!databaseVars.isEmpty()) {
                            sql = ((Map<String, Object>) v).get("sql").toString();
                            List<String> sqls = new ArrayList<>();
                            for (String var : databaseVars.keySet()) {
                                if (sql.toString().contains(String.format("{{%s}}", var))) {
                                    for (String val : databaseVars.get(var)) {
                                        sqls.add(sql.toString().replace(String.format("{{%s}}", var), val));
                                    }
                                }
                            }
                            ((Map<String, Object>) v).put("sql", sqls);
                        }
                    }
                }
            });
            databases.add(database);
        }
    }
}
