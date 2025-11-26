package org.example.inspection.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.example.inspection.service.RemoteInspectionService;
import org.example.inspection.utils.EncryptionUtil;
import org.example.inspection.utils.InspectionEntityUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class RemoteInspectionServiceImpl implements RemoteInspectionService {
    @Resource
    private InspectionEntityUtil inspectionEntityUtil;
    @Resource
    private EncryptionUtil encryptionUtil;

    private final Map<String, List<Object>> results;

    public RemoteInspectionServiceImpl() {
        results = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private void inspectionSingle(Map<String, Object> remote, String checker, String time, String date) {
        // 获取基础属性
        Map<String, Object> attrs = new HashMap<>();
        for(String k1 : remote.keySet()) {
            if(k1.startsWith("@")) {
                attrs.put(k1.substring(1), remote.get(k1));
            }
        }
        attrs.put("checker", checker);
        attrs.put("time", time);
        attrs.put("date", date);
        // 遍历服务器属性
        for (Map.Entry<String, Object> entry : remote.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (k.startsWith("@")) {
                continue;
            }
            if (!((Map<String, Object>) v).containsKey("shell")) {
                continue;
            }
            if (!results.containsKey(k)) {
                results.put(k, new ArrayList<>());
            }
            try {
                Object shello = ((Map<String, Object>) v).get("shell");
                List<String> shells;
                if (shello instanceof String) {
                    shells = Collections.singletonList(shello.toString());
                } else {
                    shells = (List<String>) shello;
                }
                for (String shell : shells) {
                    // 执行命令
                    Process process = Runtime.getRuntime().exec(shell);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    StringBuilder shellLines = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        shellLines.append(line).append(System.lineSeparator());
                    }
                    process.waitFor();
                    List<JSONObject> jsons;
                    if(shellLines.toString().startsWith("[")) {
                        jsons = JSON.parseArray(shellLines.toString(), JSONObject.class);
                    } else {
                        jsons = new ArrayList<>();
                        jsons.add(JSON.parseObject(shellLines.toString(), JSONObject.class));
                    }
                    for (JSONObject json : jsons) {
                        Map<String, Object> result;
                        // 保存别名配置
                        Map<String, String> rename = new HashMap<>();
                        if (((Map<String, Object>) v).containsKey("res")) {
                            if (((Map<String, Object>) v).get("res") instanceof List) {
                                ((List<Object>) ((Map<String, Object>) v).get("res")).forEach(r -> {
                                    Map<String, Object> m = (Map<String, Object>) r;
                                    if (m.get("@from").toString().equals(m.get("@name").toString())) {
                                        return;
                                    }
                                    rename.put(m.get("@from").toString(), m.get("@name").toString());
                                });
                            } else {
                                Map<String, Object> m = (Map<String, Object>) ((Map<String, Object>) v).get("res");
                                if (m.get("@from").toString().equals(m.get("@name").toString())) {
                                    return;
                                }
                                rename.put(m.get("@from").toString(), m.get("@name").toString());
                            }
                        }
                        // 根据别名保存结果
                        if (rename.isEmpty()) {
                            result = new LinkedHashMap<>(json);
                        } else {
                            result = new LinkedHashMap<>();
                            json.forEach((k1, v1) -> result.put(rename.getOrDefault(k1, k1), v1));
                        }
                        result.putAll(attrs);
                        // 添加阈值
                        if (((Map<String, Object>) remote.get(k)).containsKey("threshold")) {
                            Map<String, Object> threshold = new HashMap<>();
                            ((Map<String, Object>) ((Map<String, Object>) remote.get(k)).get("threshold")).forEach((k1, v1) -> {
                                Map<String, Object> item = new HashMap<>();
                                ((Map<String, Object>) v1).forEach((k2, v2) -> item.put(k2.substring(1), v2));
                                threshold.put(k1, item);
                            });
                            result.put("threshold", threshold);
                        }
                        result.remove("shell");
                        // 将结果添加到结果集中
                        results.get(k).add(result);
                    }
                }
            } catch (Exception e) {
                // 添加失败巡检的远程基础属性和失败标志
                attrs.put("fail", null);
                results.get(k).add(attrs);
            }
        }
    }
    @Override
    public Map<String, List<Object>> inspection(String checker, String time, String date) {
        List<Map<String, Object>> remotes = inspectionEntityUtil.getRemotes();
        for (Map<String, Object> remote : remotes) {
            inspectionSingle(remote, checker, time, date);
        }
        return results;
    }
}
