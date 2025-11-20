package org.example.inspection.service;

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
        List<Object> databases = inspectionEntityUtil.getDatabases();
        for (Object database : databases) {
            databaseInspectionService.inspection((Map<String, Object>) database, "神州数码", "16:00", "2025-11-19");
        }
        Map<String, List<Object>> results = databaseInspectionService.getResults();
        results.forEach((k, v) -> {
            System.out.println("============");
            System.out.println(k + ": ");
            v.forEach(System.out::println);
            System.out.println("------------");
        });
    }
}
