package org.example.inspection.service;

import com.alibaba.fastjson.JSONObject;
import org.example.inspection.TestUtil;
import org.example.inspection.utils.InspectionEntityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DatabaseInspectionServiceTest {
    @Autowired
    private DatabaseInspectionService databaseInspectionService;
    @Autowired
    private InspectionEntityUtil inspectionEntityUtil;

    @Test
    public void test() throws SQLException {
        Map<String, List<Object>> results = databaseInspectionService.inspection("神州数码", "16:00", "2025-11-19");
//        System.out.println(JSONObject.toJSONString(results));
        TestUtil.printObj(results, "");
    }
}
