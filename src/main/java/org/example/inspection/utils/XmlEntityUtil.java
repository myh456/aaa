package org.example.inspection.utils;

import lombok.Getter;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@SuppressWarnings("unchecked")
public class XmlEntityUtil {
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
            Map<String, Object> chara = (Map<String, Object>) v;
            ((Map<String, Object>) v).forEach((k1, v1) -> {
                if (k1.startsWith("@")) return;
                if (((Map<String, Object>) v1).containsKey("@id")) {
                    Integer id = Integer.parseInt(((Map<String, Object>) v1).get("@id").toString());
                    chara.put(k1, rules.get(k1).get(id));
                }
            });
            charas.put(Integer.parseInt(chara.get("@id").toString()), chara);
        });
        // 默认配置
        Map<String, Object> defaultMap = xmlParser.parseXml(configPath + "DefaultConfig.xml");
        defaultCheck = new HashMap<>();
        defaultConfig = defaultMap;
        Map<String, Object> checks = (Map<String, Object>) defaultMap.get("check");
        defaultConfig.remove("check");
        for (String k : checks.keySet()) {
            if ("character".equals(k)) {
                Object id = ((Map<String, Object>) checks.get(k)).get("@id");
                defaultCheck = (Map<String, Object>) charas.get(Integer.parseInt(id.toString()));
            } else {
                if (((Map<String, Object>) checks.get(k)).containsKey("@id")) {
                    Map<String, Object> item = (Map<String, Object>) rules.get(k).get(Integer.parseInt(((Map<String, Object>) checks.get(k)).get("@id").toString()));
                    defaultCheck.put(k, item);
                } else {
                    defaultCheck.put(k, checks.get(k));
                }
            }
        }
    }

    public void initServer() throws DocumentException {
        Map<String, Object> serversMap = xmlParser.parseXml(configPath + "ServersConfig.xml");
        servers = new ArrayList<>();
        for(Object item: (List<Object>)serversMap.get("server")) {
            Map<String, Object> check = (Map<String, Object>) item;
            Map<String, Object> server = new HashMap<>();
            boolean isDefault = true;
            for(String k : check.keySet()) {
                if(!k.startsWith("@") && ((Map<String, Object>)check.get(k)).containsKey("@id")) {
                    if("character".equals(k)) {
                        Object chara = charas.get(Integer.parseInt(((Map<String, Object>)check.get(k)).get("@id").toString()));
                        server.putAll((Map<String, Object>)chara);
                    } else {
                        server.put(k, rules.get(k).get(Integer.parseInt(((Map<String, Object>)check.get(k)).get("@id").toString())));
                    }
                    isDefault = false;
                } else {
                    server.put(k, check.get(k));
                }
            }
            if(isDefault) {
                server.putAll(defaultCheck);
            }
            // 将命令插入服务器配置
            server.forEach((k, v) -> {
                if(k.startsWith("@")) return;
                if(cmds.containsKey(k)) {
                    // 保存服务器配置的变量
                    Map<String, List<String>> serverVars = new HashMap<>();
                    ((Map<String, Object>)v).forEach((k1, v1) -> {
                        if(k1.endsWith("-attr")) {
                            if(v1 instanceof List) serverVars.put(k1.substring(0, k1.length()-5), (List<String>) v1);
                            else serverVars.put(k1.substring(0, k1.length()-5), Collections.singletonList(v1.toString()));
                        }
                    });
                    // 获取并插入命令
                    Object cmd = cmds.get(k);
                    if(cmd instanceof Map) {
                        ((Map<String, Object>) v).putAll((Map<String, Object>)cmd);
                    } else {
                        for(Object c: (List<Object>)cmd) {
                            if(((Map<String, Object>) c).get("@type").equals(server.get("@os"))) {
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
        for(Object item: (List<Object>)databaseMap.get("database")) {
            Map<String, Object> database = new HashMap<>();
            for(String k : ((Map<String, Object>) item).keySet()) {
                if(k.startsWith("@")) {
                    database.put(k, ((Map<String, Object>) item).get(k));
                    continue;
                }
                if(((Map<String, Object>) item).get(k) instanceof List) {
                    ((List<Object>) ((Map<String, Object>) item).get(k)).forEach(v -> {
                        if(sqls.containsKey(k + "#" + ((Map<String, Object>) v).get("@name"))) {
                            database.put(k + "#" + ((Map<String, Object>) v).get("@name"), sqls.get(k + "#" + ((Map<String, Object>) v).get("@name")));
                        }
                    });
                } else {
                    if (((Map<String, Object>) item).containsKey("@name") && sqls.containsKey(k + "#" + ((Map<String, Object>) item).get("@name"))) {
                        database.put(k, sqls.get(k + "#" + database.get("@name")));
                    } else if (sqls.containsKey(k)) {
                        database.put(k, sqls.get(k));
                    } else {
                        database.put(k, ((Map<String, Object>) item).get(k));
                    }
                }
            }
            databases.add(database);
        }
    }
}
