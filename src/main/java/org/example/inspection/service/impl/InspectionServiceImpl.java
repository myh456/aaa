package org.example.inspection.service.impl;

import org.example.inspection.service.DatabaseInspectionService;
import org.example.inspection.service.InspectionService;
import org.example.inspection.service.ServerInspectionService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author myh
 */
@Service
public class InspectionServiceImpl implements InspectionService {
    @Resource
    private DatabaseInspectionService databaseInspectionService;
    @Resource
    private ServerInspectionService serverInspectionService;

    private final String checker = "神州数码";
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm.ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Map<String, List<Object>> inspection(Date startTime) {
        String time = timeFormat.format(startTime);
        String date = dateFormat.format(startTime);
        Map<String, List<Object>> results = new HashMap<>();
        results.putAll(databaseInspectionService.inspection(checker, time, date));
        results.putAll(serverInspectionService.inspection(checker, time, date));
        return results;
    }
}
