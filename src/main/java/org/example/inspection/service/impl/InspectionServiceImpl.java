package org.example.inspection.service.impl;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class InspectionServiceImpl {
    private Date startTime;
    private final String checker = "神州数码";
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @PostConstruct
    public void init() {
        startTime = new Date();
    }
}
