package org.example.inspection.service.impl;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jcraft.jsch.Session;
import lombok.Getter;
import org.example.inspection.service.ServerInspectionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Service
public class ServerInspectionServiceImpl implements ServerInspectionService {

    private final Map<String, List<Object>> results;

    public ServerInspectionServiceImpl() {
        results = new HashMap<>();
    }
    @Override
    @SuppressWarnings("unchecked")
    public void inspection(Map<String, Object> server) {
        Session session = JschUtil.getSession(
                server.get("@ip").toString(),
                Integer.parseInt(server.get("@port").toString()),
                server.get("@account").toString(),
                server.get("@password").toString()
        );
        server.forEach((k, v) -> {
            if(k.startsWith("@")) return;
            if(!((Map<String, Object>) v).containsKey("cmd")) return;
            String cmd = ((Map<String, Object>) v).get("cmd").toString();
            JSONObject json = JSONObject.parseObject(JschUtil.exec(session, cmd, CharsetUtil.CHARSET_UTF_8));
            Map<String, Object> res = json.toJavaObject(new TypeReference<Map<String, Object>>(){});
            res.putAll((Map<String, Object>) server.get(k));
            res.remove("cmd");
            if(!results.containsKey(k)) results.put(k, new ArrayList<>());
            results.get(k).add(res);
        });
    }
}
