package org.example.inspection.utils;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class WordUtilTest {
    @Autowired
    private WordTextUtil wordTextUtil;

    @Test
    public void test() {
        JSONObject json = JSONObject.parseObject("{\n" +
                "  \"cpu\": {\n" +
                "    \"total\": 10,\n" +
                "    \"normal\": 8,\n" +
                "    \"fail_size\": 2,\n" +
                "    \"fail\": [\n" +
                "      {\"192.168.0.1\": null},\n" +
                "      {\"192.168.0.2\": null}\n" +
                "    ],\n" +
                "    \"over_size\": 2,\n" +
                "    \"over\": [\n" +
                "      {\"192.168.0.3\": null},\n" +
                "      {\"192.168.0.4\": null}\n" +
                "    ]\n" +
                "  },\n" +
                "  \"storage\": {\n" +
                "    \"total\": 16,\n" +
                "    \"normal\": 14,\n" +
                "    \"fail_size\": 2,\n" +
                "    \"fail\": [\n" +
                "      {\"192.168.0.1\": \"/\"},\n" +
                "      {\"192.168.0.1\": \"/weblogic\"}\n" +
                "    ],\n" +
                "    \"over_size\": 2,\n" +
                "    \"over\": [\n" +
                "      {\"192.168.0.3\": \"/\"},\n" +
                "      {\"192.168.0.3\": \"/weblogic\"}\n" +
                "    ]\n" +
                "  }\n" +
                "}");
        Map<String, Object> data = json.toJavaObject(Map.class);
        wordTextUtil.parse(data, "巡检_defrgtb");
    }

    @Test
    public void test2() {
        System.out.println(wordTextUtil.getExtract("cpu"));
        System.out.println(wordTextUtil.getExtract("storage"));
    }
}
