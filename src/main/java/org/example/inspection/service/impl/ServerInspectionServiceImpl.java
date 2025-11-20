package org.example.inspection.service.impl;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.jcraft.jsch.Session;
import lombok.Getter;
import org.example.inspection.service.ServerInspectionService;
import org.springframework.stereotype.Service;

import java.util.*;

@Getter
@Service
public class ServerInspectionServiceImpl implements ServerInspectionService {

    private final Map<String, List<Object>> results;
    public ServerInspectionServiceImpl() {
        results = new HashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void inspection(Map<String, Object> server, String checker, String time, String date) {
        // TODO: 根据配置进行ip的解密，密码的解密
        // 获取连接session
        Session session = JschUtil.getSession(
                server.get("@ip").toString(),
                Integer.parseInt(server.get("@port").toString()),
                server.get("@account").toString(),
                server.get("@password").toString()
        );
        // 获取基础属性
        Map<String, Object> attrs = new HashMap<>();
        for(String k1 : server.keySet()) {
            if(k1.startsWith("@")) attrs.put(k1.substring(1), server.get(k1));
        }
        // 遍历服务器
        server.forEach((k, v) -> {
            if(k.startsWith("@")) return;
            if(!((Map<String, Object>) v).containsKey("cmd")) return;
            // 执行命令并解析结果
            Object cmdo = ((Map<String, Object>) v).get("cmd");
            List<String> cmds;
            if(cmdo instanceof String) {
                cmds = Collections.singletonList(cmdo.toString());
            } else {
                cmds = (List<String>) cmdo;
            }
            for (String cmd : cmds) {
                JSONObject json = JSONObject.parseObject(JschUtil.exec(session, cmd, CharsetUtil.CHARSET_UTF_8), Feature.OrderedField);
                Map<String, Object> result;
                // 保存别名配置
                Map<String, String> rename = new HashMap<>();
                if (((Map<String, Object>) v).containsKey("res")) {
                    if (((Map<String, Object>) v).get("res") instanceof List) {
                        ((List<Object>) ((Map<String, Object>) v).get("res")).forEach(r -> {
                            Map<String, Object> m = (Map<String, Object>) r;
                            if (m.get("@from").toString().equals(m.get("@name").toString())) return;
                            rename.put(m.get("@from").toString(), m.get("@name").toString());
                        });
                    } else {
                        Map<String, Object> m = (Map<String, Object>) ((Map<String, Object>) v).get("res");
                        if (m.get("@from").toString().equals(m.get("@name").toString())) return;
                        rename.put(m.get("@from").toString(), m.get("@name").toString());
                    }
                }
                if(rename.isEmpty())
                    result = new LinkedHashMap<>(json);
                else {
                    result = new LinkedHashMap<>();
                    json.forEach((k1, v1) -> {
                        result.put(rename.getOrDefault(k1, k1), v1);
                    });
                }
                result.putAll(attrs);
                if(((Map<String, Object>) server.get(k)).containsKey("threshold"))
                    result.put("threshold", ((Map<String, Object>) server.get(k)).get("threshold"));
                result.remove("cmd");
                result.put("checker", checker);
                result.put("time", time);
                result.put("date", date);
                // 将结果添加到结果集中
                if(!results.containsKey(k)) results.put(k, new ArrayList<>());
                results.get(k).add(result);
            }
        });
    }
}
