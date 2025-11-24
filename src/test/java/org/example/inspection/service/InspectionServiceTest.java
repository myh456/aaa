package org.example.inspection.service;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class InspectionServiceTest {
    @Resource
    private InspectionService inspectionService;

    @Test
    public void test() {
        Map<String, List<Object>> res =  inspectionService.inspection();
        res.forEach((k, v) -> {
            System.out.println("============");
            System.out.println(k + ": ");
            v.forEach(System.out::println);
            System.out.println("------------");
        });
    }
}
