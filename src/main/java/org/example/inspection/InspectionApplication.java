package org.example.inspection;

import org.dom4j.DocumentException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InspectionApplication {

    public static void main(String[] args) throws DocumentException {
        SpringApplication.run(InspectionApplication.class, args);
    }
}
