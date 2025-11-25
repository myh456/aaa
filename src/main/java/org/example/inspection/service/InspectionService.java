package org.example.inspection.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author myh
 */
public interface InspectionService {

    Map<String, List<Object>> inspection(Date startTime);
}
