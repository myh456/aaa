package org.example.inspection.service;

import org.example.inspection.InspectionApplication;
import org.example.inspection.utils.XmlEntityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;


@SpringBootTest(classes = InspectionApplication.class)
@RunWith(SpringRunner.class)
public class ServerInspectionServiceTest {
    @Autowired
    private ServerInspectionService serverInspectionService;
    @Autowired
    private XmlEntityUtil xmlEntityUtil;

    @Test
    public void test() {
        List<Object> servers = xmlEntityUtil.getServers();
        for (Object server : servers) {
            serverInspectionService.inspection((Map<String, Object>) server);
        }
        Map<String, List<Object>> results = serverInspectionService.getResults();
        results.forEach((k, v) -> {
            System.out.println("============");
            System.out.println(k + ": ");
            v.forEach(System.out::println);
            System.out.println("------------");
        });
    }
}
