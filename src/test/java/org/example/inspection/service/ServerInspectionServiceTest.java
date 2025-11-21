package org.example.inspection.service;

import com.alibaba.fastjson.JSONObject;
import org.example.inspection.utils.InspectionEntityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;


@SpringBootTest
@RunWith(SpringRunner.class)
public class ServerInspectionServiceTest {
    @Autowired
    private ServerInspectionService serverInspectionService;

    @Test
    public void test() {
        Map<String, List<Object>> results = serverInspectionService.inspection("神州数码", "16:00", "2025-11-19");
        JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(results));
        System.out.println(json.toJSONString());
//        results.forEach((k, v) -> {
//            System.out.println("============");
//            System.out.println(k + ": ");
//            v.forEach(System.out::println);
//            System.out.println("------------");
//        });
    }
}
