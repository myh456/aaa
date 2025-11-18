package org.example.inspection.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class XmlEntityUtilTest {
    @Autowired
    private XmlEntityUtil xmlEntityUtil;

    @Test
    public void test() {
        System.out.println(xmlEntityUtil.getServers());
    }
}
