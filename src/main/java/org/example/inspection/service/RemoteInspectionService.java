package org.example.inspection.service;

import java.util.List;
import java.util.Map;

public interface RemoteInspectionService {

    Map<String, List<Object>> inspection(String checker, String time, String date);
}
