package org.example.inspection.service;

import java.util.List;
import java.util.Map;

/**
 * @author myh
 */
public interface InspectionService {

    void init();

    Map<String, List<Object>> inspection();
}
