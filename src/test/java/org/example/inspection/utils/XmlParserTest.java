package org.example.inspection.utils;

import org.dom4j.DocumentException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@RunWith(SpringRunner.class)
public class XmlParserTest {
    @Autowired
    private XmlParser xmlParser;

    @Test
    public void test() throws DocumentException {
        Map<String, Object> cmds = xmlParser.parseXml("classpath:config/system/ServersRunning.xml");
        System.out.println(cmds);

        Map<String, Object> servers = xmlParser.parseXml("classpath:config/ServersConfig.xml");
        ((List<Object>) servers.get("server")).forEach((v) -> {
            if (v instanceof Map) {
                Map<String, Object> items = (Map<String, Object>) v;
                items.forEach((k1, v1) -> {
                    if (k1.startsWith("@")) return;
                    if (cmds.containsKey(k1)) {
                        Object cmd = cmds.get(k1);
                        String sh = null;
                        if (cmd instanceof Map) {
                            sh = (String.valueOf(((Map<String, Object>) cmd).get("cmd")));
                        } else {
                            for (Object c : (List<Object>) cmd) {
                                if (((Map<String, Object>) items).get("@os").equals(((Map<String, Object>) c).get("@type"))) {
                                    sh = (String.valueOf(((Map<String, Object>) c).get("cmd")));
                                }
                            }
                        }
                        if (sh == null) return;
                        Map<String, String> env = new HashMap<>();
                        ((Map<String, Object>) v1).forEach((k2, v2) -> {
                            if (v2 instanceof String) {
                                env.put(k2, (String) v2);
                            }
                        });
                        Pattern pattern = Pattern.compile("\\{\\{(.+?)\\}\\}");
                        Matcher matcher = pattern.matcher(sh);

                        StringBuffer result = new StringBuffer();
                        while (matcher.find()) {
                            String key = matcher.group(1);
                            String replacement = env.getOrDefault(key, "{{" + key + "}}");
                            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                        }
                        matcher.appendTail(result);
                        System.out.println("已执行" + result);
                    }
                });
            }
        });
    }

    @Test
    public void test1() throws DocumentException {
        Map<String, Object> rulesMap = xmlParser.parseXml("classpath:config/RulesConfig.xml");
        Map<String, Map<Integer, Object>> rules = new HashMap<>();
        rulesMap.forEach((k, v) -> {
            rules.put(k, new HashMap<>());
            ((List<Object>) v).forEach((v1) -> {
                rules.get(k).put(Integer.parseInt(((Map<String, Object>) v1).get("@id").toString()), v1);
            });
        });
        System.out.println(rules);
        Map<String, Object> charasMap = xmlParser.parseXml("classpath:config/CharacterConfig.xml");
        Map<Integer, Object> charas = new HashMap<>();
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
        System.out.println(charas);
        Map<String, Object> defaultMap = xmlParser.parseXml("classpath:config/DefaultConfig.xml");
        Map<String, Object> defaults = new HashMap<>();
        Map<String, Object> checks = (Map<String, Object>) defaultMap.get("check");
        for (String k : checks.keySet()) {
            if ("character".equals(k)) {
                Object id = ((Map<String, Object>) ((Map<String, Object>) checks.get(k))).get("@id");
                defaults = (Map<String, Object>) charas.get(Integer.parseInt(id.toString()));
                defaults.remove("@id");
            } else {
                if (((Map<String, Object>) checks.get(k)).containsKey("@id")) {
                    Map<String, Object> item = (Map<String, Object>) rules.get(k).get(Integer.parseInt(((Map<String, Object>) checks.get(k)).get("@id").toString()));
                    defaults.put(k, item);
                } else {
                    defaults.put(k, checks.get(k));
                }
            }
        }
        Map<String, Object> serversMap = xmlParser.parseXml("classpath:config/ServersConfig.xml");
        List<Object> servers = new ArrayList<>();
        Map<String, Object> finalDefaults = defaults;
        ((List<Object>)serversMap.get("server")).forEach((item) -> {
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
                server.putAll(finalDefaults);
            }
            servers.add(server);
        });
        System.out.println(servers);
    }

    @Test
    public void test2() throws DocumentException {
        Map<String, Object> wordText = xmlParser.parseXml("classpath:config/system/WordText.xml");
        System.out.println(wordText);
    }
}
