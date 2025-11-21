package org.example.inspection.service.impl;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.jcraft.jsch.Session;
import org.example.inspection.service.ServerInspectionService;
import org.example.inspection.utils.EncryptionUtil;
import org.example.inspection.utils.InspectionEntityUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 服务器巡检服务类
 * @author myh
 */
@Service
public class ServerInspectionServiceImpl implements ServerInspectionService {

    @Resource
    private InspectionEntityUtil inspectionEntityUtil;
    @Resource
    private EncryptionUtil encryptionUtil;

    private final Map<String, List<Object>> results;

    public ServerInspectionServiceImpl() {
        results = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private void inspectionSingle(Map<String, Object> server, String checker, String time, String date) {
        // 获取基础属性
        Map<String, Object> attrs = new HashMap<>();
        for(String k1 : server.keySet()) {
            if(k1.startsWith("@")) {
                attrs.put(k1.substring(1), server.get(k1));
            }
        }
        attrs.put("checker", checker);
        attrs.put("time", time);
        attrs.put("date", date);
        String ip = attrs.get("ip").toString();
        if("true".equals((inspectionEntityUtil.getDefaultConfig()).get("ip_enc"))) {
            ip = encryptionUtil.decrypt(ip);
        }
        int port = Integer.parseInt(attrs.get("port").toString());
        String account = attrs.get("account").toString();
        String password = encryptionUtil.decrypt(attrs.get("password").toString());
        // 获取连接session
        Session session = null;
        try {
            session = JschUtil.getSession(ip, port, account, password);
        } catch (Exception e) {
            System.err.println("连接失败: " + attrs.get("name"));
        }
        // 遍历服务器属性
        for (Map.Entry<String, Object> entry : server.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (k.startsWith("@")) {
                continue;
            }
            if (!((Map<String, Object>) v).containsKey("cmd")) {
                continue;
            }
            if (!results.containsKey(k)) {
                results.put(k, new ArrayList<>());
            }
            try {
                // 获取命令列表
                Object cmdo = ((Map<String, Object>) v).get("cmd");
                List<String> cmds;
                if (cmdo instanceof String) {
                    cmds = Collections.singletonList(cmdo.toString());
                } else {
                    cmds = (List<String>) cmdo;
                }
                for (String cmd : cmds) {
                    // 执行命令
                    String cmdRes = JschUtil.exec(session, cmd, CharsetUtil.CHARSET_UTF_8);
                    List<JSONObject> jsons;
                    if(cmdRes.startsWith("[")) {
                        jsons = JSON.parseArray(cmdRes, JSONObject.class);
                    } else {
                        jsons = new ArrayList<>();
                        jsons.add(JSON.parseObject(cmdRes, JSONObject.class));
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
                        if (((Map<String, Object>) server.get(k)).containsKey("threshold")) {
                            Map<String, Object> threshold = new HashMap<>();
                            ((Map<String, Object>) ((Map<String, Object>) server.get(k)).get("threshold")).forEach((k1, v1) -> {
                                Map<String, Object> item = new HashMap<>();
                                ((Map<String, Object>) v1).forEach((k2, v2) -> item.put(k2.substring(1), v2));
                                threshold.put(k1, item);
                            });
                            result.put("threshold", threshold);
                        }
                        result.remove("cmd");
                        // 将结果添加到结果集中
                        results.get(k).add(result);
                    }
                }
            } catch (Exception e) {
                // 添加失败巡检的服务器基础属性
                results.get(k).add(attrs);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, List<Object>> inspection(String checker, String time, String date) {
        List<Object> servers = inspectionEntityUtil.getServers();
        for (Object server : servers) {
            inspectionSingle((Map<String, Object>) server, checker, time, date);
        }
        return results;
    }
}
