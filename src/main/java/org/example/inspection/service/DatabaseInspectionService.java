package org.example.inspection.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseInspectionService {

    void inspection(Map<String, Object> database, String checker, String time, String date) throws SQLException;

    Map<String, List<Object>> getResults();
}
