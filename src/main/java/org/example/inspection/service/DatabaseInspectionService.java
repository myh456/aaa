package org.example.inspection.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseInspectionService {

    Map<String, List<Object>> inspection(String checker, String time, String date);
}
