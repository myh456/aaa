package org.example.inspection.service;

import java.util.List;
import java.util.Map;

public interface ServerInspectionService {

    void inspection(Map<String, Object> server);

    Map<String, List<Object>> getResults();
}
