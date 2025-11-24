package org.example.inspection;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DataSourceTest {
    @Test
    public void test() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        // clickhouse
        dataSource.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        dataSource.setUrl("jdbc:clickhouse://127.0.0.1:8123");
        dataSource.setUsername("default");
        dataSource.setPassword("456456xx");
        if (dataSource.getConnection() != null) {
            System.out.println("连接成功");
        } else {
            System.out.println("连接失败");
        }
    }
}
