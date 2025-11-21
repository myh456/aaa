package org.example.inspection.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.example.inspection.utils.InspectionEntityUtil;
import org.example.inspection.utils.ExcelHeaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.util.*;

@RestController
public class TestController {

    @Resource
    private InspectionEntityUtil xmlEntityUtil;

    @Value("${excel.export.path}")
    private String excelExportPath;

    @GetMapping("/test")
    public void test(HttpServletResponse response) {
        System.out.println("11111111111111111111111111111111111111");

        try {
            // 生成带时间戳的文件名
            String timestamp = DateUtil.format(new Date(), "yyyyMMddHHmm");
            String fileName = "巡检_" + timestamp + ".xlsx";
            String filePath = excelExportPath + fileName;

            // 确保目录存在
            File directory = new File(excelExportPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 如果文件已存在，则先删除
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }

            ExcelWriter writer = ExcelUtil.getWriter(filePath);
            // 删除默认的sheet1工作表
            writer.getWorkbook().removeSheetAt(0);

            // 获取excel配置
            List<Object> excelConfigs = xmlEntityUtil.getExcelConfigs();
            // 获取所有表头
            Map<String, Map<String, String>> map = xmlEntityUtil.extractExcelHeaders();
            // 获取所有表头配置（包括列宽和合并单元格信息）
            Map<String, ExcelHeaderConfig> headerConfigs = xmlEntityUtil.extractExcelHeaderConfigs();
            // 巡检结果
            String result = "{\"cpu\":[{\"us\":80,\"sy\":1.2,\"id\":\"1\",\"wa\":0.0,\"password\":\"456456xx\",\"os\":\"linux\",\"port\":\"2222\",\"ip\":\"127.0.0.1\",\"name\":\"WSL\",\"account\":\"myh\",\"threshold\":{\"id\":{\"lower\":\"20\"},\"us\":{\"higher\":\"80\"}},\"checker\":\"神州数码\",\"time\":\"10:16\",\"date\":\"2025-11-21\"},{\"us\":0.0,\"sy\":0.0,\"id\":\"2\",\"wa\":0.0,\"password\":\"456456xx\",\"os\":\"linux\",\"port\":\"2222\",\"ip\":\"127.0.0.1\",\"name\":\"WSL1\",\"account\":\"myh\",\"threshold\":{\"id\":{\"lower\":\"20\"},\"us\":{\"higher\":\"80\"}},\"checker\":\"神州数码\",\"time\":\"10:16\",\"date\":\"2025-11-21\"}],\"service-zt\":[{\"host\":\"localhost\",\"db\":\"performance_schema\",\"user\":\"mysql.session\",\"select_priv\":\"Y\",\"insert_priv\":\"N\",\"update_priv\":\"N\",\"delete_priv\":\"N\",\"create_priv\":\"N\",\"drop_priv\":\"N\",\"grant_priv\":\"N\",\"references_priv\":\"N\",\"index_priv\":\"N\",\"alter_priv\":\"N\",\"create_tmp_table_priv\":\"N\",\"lock_tables_priv\":\"N\",\"create_view_priv\":\"N\",\"show_view_priv\":\"N\",\"create_routine_priv\":\"N\",\"alter_routine_priv\":\"N\",\"execute_priv\":\"N\",\"event_priv\":\"N\",\"trigger_priv\":\"N\",\"failed\":7.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库1\",\"id\":\"3\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}},{\"host\":\"localhost\",\"db\":\"sys\",\"user\":\"mysql.sys\",\"select_priv\":\"N\",\"insert_priv\":\"N\",\"update_priv\":\"N\",\"delete_priv\":\"N\",\"create_priv\":\"N\",\"drop_priv\":\"N\",\"grant_priv\":\"N\",\"references_priv\":\"N\",\"index_priv\":\"N\",\"alter_priv\":\"N\",\"create_tmp_table_priv\":\"N\",\"lock_tables_priv\":\"N\",\"create_view_priv\":\"N\",\"show_view_priv\":\"N\",\"create_routine_priv\":\"N\",\"alter_routine_priv\":\"N\",\"execute_priv\":\"N\",\"event_priv\":\"N\",\"trigger_priv\":\"Y\",\"failed\":1.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库1\",\"id\":\"3\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}},{\"host\":\"localhost\",\"db\":\"performance_schema\",\"user\":\"mysql.session\",\"select_priv\":\"Y\",\"insert_priv\":\"N\",\"update_priv\":\"N\",\"delete_priv\":\"N\",\"create_priv\":\"N\",\"drop_priv\":\"N\",\"grant_priv\":\"N\",\"references_priv\":\"N\",\"index_priv\":\"N\",\"alter_priv\":\"N\",\"create_tmp_table_priv\":\"N\",\"lock_tables_priv\":\"N\",\"create_view_priv\":\"N\",\"show_view_priv\":\"N\",\"create_routine_priv\":\"N\",\"alter_routine_priv\":\"N\",\"execute_priv\":\"N\",\"event_priv\":\"N\",\"trigger_priv\":\"N\",\"failed\":1.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库2\",\"id\":\"4\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}},{\"host\":\"localhost\",\"db\":\"sys\",\"user\":\"mysql.sys\",\"select_priv\":\"N\",\"insert_priv\":\"N\",\"update_priv\":\"N\",\"delete_priv\":\"N\",\"create_priv\":\"N\",\"drop_priv\":\"N\",\"grant_priv\":\"N\",\"references_priv\":\"N\",\"index_priv\":\"N\",\"alter_priv\":\"N\",\"create_tmp_table_priv\":\"N\",\"lock_tables_priv\":\"N\",\"create_view_priv\":\"N\",\"show_view_priv\":\"N\",\"create_routine_priv\":\"N\",\"alter_routine_priv\":\"N\",\"execute_priv\":\"N\",\"event_priv\":\"N\",\"trigger_priv\":\"Y\",\"failed\":4.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库2\",\"id\":\"4\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}},{\"host\":\"localhost\",\"user\":\"myh\",\"select_priv\":\"N\",\"insert_priv\":\"N\",\"update_priv\":\"N\",\"delete_priv\":\"N\",\"create_priv\":\"N\",\"drop_priv\":\"N\",\"reload_priv\":\"N\",\"shutdown_priv\":\"N\",\"process_priv\":\"N\",\"file_priv\":\"N\",\"grant_priv\":\"N\",\"references_priv\":\"N\",\"index_priv\":\"N\",\"alter_priv\":\"N\",\"show_db_priv\":\"N\",\"super_priv\":\"N\",\"create_tmp_table_priv\":\"N\",\"lock_tables_priv\":\"N\",\"execute_priv\":\"N\",\"repl_slave_priv\":\"N\",\"repl_client_priv\":\"N\",\"create_view_priv\":\"N\",\"show_view_priv\":\"N\",\"create_routine_priv\":\"N\",\"alter_routine_priv\":\"N\",\"create_user_priv\":\"N\",\"event_priv\":\"N\",\"trigger_priv\":\"N\",\"create_tablespace_priv\":\"N\",\"ssl_type\":\"\",\"ssl_cipher\":\"\",\"x509_issuer\":\"\",\"x509_subject\":\"\",\"max_questions\":0,\"max_updates\":0,\"max_connections\":0,\"max_user_connections\":0,\"plugin\":\"caching_sha2_password\",\"authentication_string\":\"$A$005$&h\\u0001g\\u0001\\n\\u00188~\\u001A\\u0006nCD\\u0018d.KY\\u0010UrJkAMqWVdcjDWSiGqT4GN1glvdYRb3U9kNhX3nWe66\",\"password_expired\":\"N\",\"password_last_changed\":1763625811000,\"password_lifetime\":30,\"account_locked\":\"N\",\"create_role_priv\":\"N\",\"drop_role_priv\":\"N\",\"failed\":7.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库2\",\"id\":\"4\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}},{\"host\":\"localhost\",\"user\":\"mysql.infoschema\",\"select_priv\":\"Y\",\"insert_priv\":\"N\",\"update_priv\":\"N\",\"delete_priv\":\"N\",\"create_priv\":\"N\",\"drop_priv\":\"N\",\"reload_priv\":\"N\",\"shutdown_priv\":\"N\",\"process_priv\":\"N\",\"file_priv\":\"N\",\"grant_priv\":\"N\",\"references_priv\":\"N\",\"index_priv\":\"N\",\"alter_priv\":\"N\",\"show_db_priv\":\"N\",\"super_priv\":\"N\",\"create_tmp_table_priv\":\"N\",\"lock_tables_priv\":\"N\",\"execute_priv\":\"N\",\"repl_slave_priv\":\"N\",\"repl_client_priv\":\"N\",\"create_view_priv\":\"N\",\"show_view_priv\":\"N\",\"create_routine_priv\":\"N\",\"alter_routine_priv\":\"N\",\"create_user_priv\":\"N\",\"event_priv\":\"N\",\"trigger_priv\":\"N\",\"create_tablespace_priv\":\"N\",\"ssl_type\":\"\",\"ssl_cipher\":\"\",\"x509_issuer\":\"\",\"x509_subject\":\"\",\"max_questions\":0,\"max_updates\":0,\"max_connections\":0,\"max_user_connections\":0,\"plugin\":\"caching_sha2_password\",\"authentication_string\":\"$A$005$THISISACOMBINATIONOFINVALIDSALTANDPASSWORDTHATMUSTNEVERBRBEUSED\",\"password_expired\":\"N\",\"password_last_changed\":1751350939000,\"account_locked\":\"Y\",\"create_role_priv\":\"N\",\"drop_role_priv\":\"N\",\"failed\":4.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库2\",\"id\":\"4\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}},{\"host\":\"localhost\",\"user\":\"mysql.session\",\"select_priv\":\"N\",\"insert_priv\":\"N\",\"update_priv\":\"N\",\"delete_priv\":\"N\",\"create_priv\":\"N\",\"drop_priv\":\"N\",\"reload_priv\":\"N\",\"shutdown_priv\":\"Y\",\"process_priv\":\"N\",\"file_priv\":\"N\",\"grant_priv\":\"N\",\"references_priv\":\"N\",\"index_priv\":\"N\",\"alter_priv\":\"N\",\"show_db_priv\":\"N\",\"super_priv\":\"Y\",\"create_tmp_table_priv\":\"N\",\"lock_tables_priv\":\"N\",\"execute_priv\":\"N\",\"repl_slave_priv\":\"N\",\"repl_client_priv\":\"N\",\"create_view_priv\":\"N\",\"show_view_priv\":\"N\",\"create_routine_priv\":\"N\",\"alter_routine_priv\":\"N\",\"create_user_priv\":\"N\",\"event_priv\":\"N\",\"trigger_priv\":\"N\",\"create_tablespace_priv\":\"N\",\"ssl_type\":\"\",\"ssl_cipher\":\"\",\"x509_issuer\":\"\",\"x509_subject\":\"\",\"max_questions\":0,\"max_updates\":0,\"max_connections\":0,\"max_user_connections\":0,\"plugin\":\"caching_sha2_password\",\"authentication_string\":\"$A$005$THISISACOMBINATIONOFINVALIDSALTANDPASSWORDTHATMUSTNEVERBRBEUSED\",\"password_expired\":\"N\",\"password_last_changed\":1751350939000,\"account_locked\":\"Y\",\"create_role_priv\":\"N\",\"drop_role_priv\":\"N\",\"failed\":7.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库2\",\"id\":\"4\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}},{\"host\":\"localhost\",\"user\":\"mysql.sys\",\"select_priv\":\"N\",\"insert_priv\":\"N\",\"update_priv\":\"N\",\"delete_priv\":\"N\",\"create_priv\":\"N\",\"drop_priv\":\"N\",\"reload_priv\":\"N\",\"shutdown_priv\":\"N\",\"process_priv\":\"N\",\"file_priv\":\"N\",\"grant_priv\":\"N\",\"references_priv\":\"N\",\"index_priv\":\"N\",\"alter_priv\":\"N\",\"show_db_priv\":\"N\",\"super_priv\":\"N\",\"create_tmp_table_priv\":\"N\",\"lock_tables_priv\":\"N\",\"execute_priv\":\"N\",\"repl_slave_priv\":\"N\",\"repl_client_priv\":\"N\",\"create_view_priv\":\"N\",\"show_view_priv\":\"N\",\"create_routine_priv\":\"N\",\"alter_routine_priv\":\"N\",\"create_user_priv\":\"N\",\"event_priv\":\"N\",\"trigger_priv\":\"N\",\"create_tablespace_priv\":\"N\",\"ssl_type\":\"\",\"ssl_cipher\":\"\",\"x509_issuer\":\"\",\"x509_subject\":\"\",\"max_questions\":0,\"max_updates\":0,\"max_connections\":0,\"max_user_connections\":0,\"plugin\":\"caching_sha2_password\",\"authentication_string\":\"$A$005$THISISACOMBINATIONOFINVALIDSALTANDPASSWORDTHATMUSTNEVERBRBEUSED\",\"password_expired\":\"N\",\"password_last_changed\":1751350939000,\"account_locked\":\"Y\",\"create_role_priv\":\"N\",\"drop_role_priv\":\"N\",\"failed\":4.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库2\",\"id\":\"4\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}},{\"host\":\"localhost\",\"user\":\"root\",\"select_priv\":\"Y\",\"insert_priv\":\"Y\",\"update_priv\":\"Y\",\"delete_priv\":\"Y\",\"create_priv\":\"Y\",\"drop_priv\":\"Y\",\"reload_priv\":\"Y\",\"shutdown_priv\":\"Y\",\"process_priv\":\"Y\",\"file_priv\":\"Y\",\"grant_priv\":\"Y\",\"references_priv\":\"Y\",\"index_priv\":\"Y\",\"alter_priv\":\"Y\",\"show_db_priv\":\"Y\",\"super_priv\":\"Y\",\"create_tmp_table_priv\":\"Y\",\"lock_tables_priv\":\"Y\",\"execute_priv\":\"Y\",\"repl_slave_priv\":\"Y\",\"repl_client_priv\":\"Y\",\"create_view_priv\":\"Y\",\"show_view_priv\":\"Y\",\"create_routine_priv\":\"Y\",\"alter_routine_priv\":\"Y\",\"create_user_priv\":\"Y\",\"event_priv\":\"Y\",\"trigger_priv\":\"Y\",\"create_tablespace_priv\":\"Y\",\"ssl_type\":\"\",\"ssl_cipher\":\"\",\"x509_issuer\":\"\",\"x509_subject\":\"\",\"max_questions\":0,\"max_updates\":0,\"max_connections\":0,\"max_user_connections\":0,\"plugin\":\"caching_sha2_password\",\"authentication_string\":\"$A$005$d[;w_e\\u001BH~Ra\\u000B\\\"4\\\"M{a@ovlNZ3bNolF1kZPtrgTSfJN4EyJ6LO1EdNSC1rXa1Pk2\",\"password_expired\":\"N\",\"password_last_changed\":1751351054000,\"account_locked\":\"N\",\"create_role_priv\":\"Y\",\"drop_role_priv\":\"Y\",\"failed\":8.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库2\",\"id\":\"4\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}}],\"memory\":[{\"mem_total\":\"8011996\",\"mem_used\":\"517944\",\"mem_free\":\"7450832\",\"free_total\":\"2097152\",\"free_used\":\"0\",\"free_free\":\"2097152\",\"password\":\"456456xx\",\"os\":\"linux\",\"port\":\"2222\",\"ip\":\"127.0.0.1\",\"name\":\"WSL\",\"id\":\"1\",\"account\":\"myh\",\"threshold\":{\"mem_used\":{\"higher\":\"80\"},\"free_used\":{\"higher\":\"10\"}},\"checker\":\"神州数码\",\"time\":\"10:16\",\"date\":\"2025-11-21\"},{\"mem_total\":\"8011996\",\"mem_used\":\"521212\",\"mem_free\":\"7447564\",\"free_total\":\"2097152\",\"free_used\":\"0\",\"free_free\":\"2097152\",\"password\":\"456456xx\",\"os\":\"linux\",\"port\":\"2222\",\"ip\":\"127.0.0.1\",\"name\":\"WSL1\",\"id\":\"2\",\"account\":\"myh\",\"threshold\":{\"mem_used\":{\"higher\":\"85\"},\"free_used\":{\"higher\":\"10\"}},\"checker\":\"神州数码\",\"time\":\"10:16\",\"date\":\"2025-11-21\"},{\"password\":\"456456xx\",\"os\":\"linux\",\"port\":\"2223\",\"ip\":\"127.0.0.1\",\"name\":\"WSL2\",\"account\":\"myh\"}],\"storage\":[{\"filesystem_/\":\"/dev/sdd\",\"size_/\":\"1055762868\",\"used_/\":\"2430140\",\"avail_/\":\"999629256\",\"use_root\":\"1\",\"mounted_/\":\"/\",\"dir_/\":\"/\",\"filesystem_/home\":\"/dev/sdd\",\"size_/home\":\"1055762868\",\"used_/home\":\"2430140\",\"avail_/home\":\"999629256\",\"use_home\":\"1\",\"mounted_/home\":\"/\",\"dir_/home\":\"/home\",\"filesystem_/opt\":\"/dev/sdd\",\"size_/opt\":\"1055762868\",\"used_/opt\":\"2430140\",\"avail_/opt\":\"999629256\",\"use_/opt\":\"1\",\"mounted_/opt\":\"/\",\"dir_/opt\":\"/opt\",\"password\":\"456456xx\",\"os\":\"linux\",\"port\":\"2222\",\"ip\":\"127.0.0.1\",\"name\":\"WSL1\",\"id\":\"2\",\"account\":\"myh\",\"threshold\":{\"use_root\":{\"higher\":\"80\"},\"use_home\":{\"higher\":\"80\"}},\"checker\":\"神州数码\",\"time\":\"10:16\",\"date\":\"2025-11-21\"}],\"service-sb\":[{\"host\":\"localhost\",\"db\":\"performance_schema\",\"user\":\"mysql.session\",\"failed\":4.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库1\",\"id\":\"3\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}},{\"host\":\"localhost\",\"db\":\"sys\",\"user\":\"mysql.sys\",\"failed\":5.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库1\",\"id\":\"3\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}},{\"host\":\"localhost\",\"db\":\"performance_schema\",\"user\":\"mysql.session\",\"failed\":8.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库2\",\"id\":\"4\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}},{\"host\":\"localhost\",\"db\":\"sys\",\"user\":\"mysql.sys\",\"failed\":4.0,\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库2\",\"id\":\"4\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"failed\":{\"higher\":\"6\"}}}],\"expire-mysql\":[{\"user\":\"myh\",\"host\":\"localhost\",\"password_last_changed\":1763625811000,\"password_lifetime\":\"30\",\"password_expired\":\"N\",\"expire_time\":\"2025-12-20 16:03:31\",\"remaining_days\":\"29\",\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库1\",\"id\":\"3\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"remaining_days\":{\"lower\":\"7\"}}},{\"user\":\"mysql.infoschema\",\"host\":\"localhost\",\"password_last_changed\":1751350939000,\"password_lifetime\":\"\",\"password_expired\":\"N\",\"expire_time\":\"\",\"remaining_days\":\"\",\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库1\",\"id\":\"3\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"remaining_days\":{\"lower\":\"7\"}}},{\"user\":\"mysql.session\",\"host\":\"localhost\",\"password_last_changed\":1751350939000,\"password_lifetime\":\"\",\"password_expired\":\"N\",\"expire_time\":\"\",\"remaining_days\":\"\",\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库1\",\"id\":\"3\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"remaining_days\":{\"lower\":\"7\"}}},{\"user\":\"mysql.sys\",\"host\":\"localhost\",\"password_last_changed\":1751350939000,\"password_lifetime\":\"\",\"password_expired\":\"N\",\"expire_time\":\"\",\"remaining_days\":\"\",\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库1\",\"id\":\"3\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"remaining_days\":{\"lower\":\"7\"}}},{\"user\":\"root\",\"host\":\"localhost\",\"password_last_changed\":1751351054000,\"password_lifetime\":\"\",\"password_expired\":\"N\",\"expire_time\":\"\",\"remaining_days\":\"\",\"password\":\"456456xx\",\"port\":\"3306\",\"ip\":\"127.0.0.1\",\"name\":\"数据库1\",\"id\":\"3\",\"type\":\"mysql8\",\"username\":\"root\",\"threshold\":{\"remaining_days\":{\"lower\":\"7\"}}}]}";
            JSONObject json = JSONUtil.parseObj(result);

            for (int i = 0; i < excelConfigs.size(); i++) {
                Map<String, Object> excelConfig = (Map<String, Object>) excelConfigs.get(i);
                String sheetName = MapUtil.getStr(excelConfig, "@name", "OP");
                writer.setSheet(sheetName);

                Object tableObj = excelConfig.get("table");
                if (tableObj instanceof Map) {
                    // 单表情况
                    Map<String, Object> tableMap = (Map<String, Object>) tableObj;
                    String forKey = MapUtil.getStr(tableMap, "@for");
                    String title = MapUtil.getStr(tableMap, "title");

                    int currentRow = 0; // 跟踪当前行位置

                    // 根据table的@for的值在表头的map中取出表头
                    Map<String, String> headerMap = map.get(forKey);
                    ExcelHeaderConfig headerConfig = headerConfigs.get(forKey);
                    if (headerMap != null) {
                        // 添加标题行
                        if (title != null && !title.trim().isEmpty()) {
                            int headerWidth = headerMap.size() - 1; // 0-based index
                            org.example.inspection.utils.ExcelUtil.addTitleRow(writer, title.trim(), currentRow, 0, headerWidth);
                            currentRow++; // 标题行占用一行
                        }

                        // 设置表头别名
                        org.example.inspection.utils.ExcelUtil.setHeaderAlias(writer, headerMap);

                        // 设置表头样式
                        org.example.inspection.utils.ExcelUtil.setHeaderStyle(writer);
                        
                        // 设置列宽
                        if (headerConfig != null) {
                            org.example.inspection.utils.ExcelUtil.setColumnWidths(writer, headerConfig);
                        }

                        // 数据在json中取出
                        // 注意：这里应该使用tableMap的@for属性值来获取数据
                        Object tableData = json.get(forKey);
                        // 即使没有数据也要写入表头
                        if (tableData instanceof List) {
                            List<?> dataList = (List<?>) tableData;
                            // 确保数据按照headerMap的字段顺序显示
                            List<Map<String, Object>> orderedDataList = new ArrayList<>();
                            Object thresholdObj = ((Map<String, Object>) dataList.get(0)).get("threshold");
                            for (Object dataItem : dataList) {
                                Map<String, Object> orderedItem = new LinkedHashMap<>();
                                if (dataItem instanceof Map) {
                                    // 严格按照headerMap的顺序和字段来生成表头和数据
                                    for (String field : headerMap.keySet()) {
                                        orderedItem.put(field.toLowerCase(), ((Map<String, Object>) dataItem).get(field.toLowerCase()));
                                    }
                                } else {
                                    // 如果不是Map类型，创建一个只包含headerMap字段的空对象
                                    for (String field : headerMap.keySet()) {
                                        orderedItem.put(field.toLowerCase(), "");
                                    }
                                }
                                orderedDataList.add(orderedItem);
                            }
                            // Using passRows to ensure data is written to the correct position
                            writer.passRows(currentRow);
                            writer.write(orderedDataList, true);

                            // Apply threshold highlighting for each data row
                            for (int rowIndex = 0; rowIndex < orderedDataList.size(); rowIndex++) {
                                Map<String, Object> rowData = orderedDataList.get(rowIndex);
                                int actualRow = currentRow + rowIndex + 1; // +1 for header row

                                // Check each field against its threshold
                                int colIndex = 0;
                                for (String fieldName : headerMap.keySet()) {
                                    org.example.inspection.utils.ExcelUtil.checkThresholdAndHighlight(
                                        writer, rowData, thresholdObj, fieldName, colIndex, actualRow);
                                    colIndex++;
                                }
                            }

                            currentRow += orderedDataList.size() + 1; // +1 for header
                        } else if (tableData != null) {
                            List<Map<String, Object>> dataList = new ArrayList<>();
                            // 确保数据按照headerMap的字段顺序显示
                            if (tableData instanceof Map) {
                                Map<String, Object> orderedItem = new LinkedHashMap<>();
                                // 严格按照headerMap的顺序和字段来生成表头和数据
                                for (String field : headerMap.keySet()) {
                                    orderedItem.put(field.toLowerCase(), ((Map<String, Object>) tableData).get(field.toLowerCase()));
                                }
                                dataList.add(orderedItem);
                            } else {
                                // 如果不是Map类型，创建一个只包含headerMap字段的空对象
                                Map<String, Object> orderedItem = new LinkedHashMap<>();
                                for (String field : headerMap.keySet()) {
                                    orderedItem.put(field.toLowerCase(), "");
                                }
                                dataList.add(orderedItem);
                            }
                            // 使用passRows确保数据写入到正确的位置
                            writer.passRows(currentRow);
                            writer.write(dataList, true);
                            currentRow += 2; // Single object + header
                        } else {
                            // 即使data为null也要写入表头
                            // 创建一个包含所有表头字段的空对象
                            Map<String, Object> emptyRow = new LinkedHashMap<>();
                            for (String field : headerMap.keySet()) {
                                emptyRow.put(field.toLowerCase(), "");
                            }
                            List<Map<String, Object>> emptyList = new ArrayList<>();
                            emptyList.add(emptyRow);
                            // 使用passRows确保数据写入到正确的位置
                            writer.passRows(currentRow);
                            writer.write(emptyList, true);
                            currentRow += 1; // Only header
                        }
                    }
                } else if (tableObj instanceof List) {
                    // 多表情况
                    List<Map<String, Object>> tableMaps = (List<Map<String, Object>>) tableObj;
                    boolean isFirstTable = true;
                    int currentRow = 0; // 跟踪当前行位置

                    for (Map<String, Object> table : tableMaps) {
                        if (!isFirstTable) {
                            // 在每个表格之间间隔2空行
                            writer.passRows(2);
                            currentRow += 2;
                        }
                        String forKey = MapUtil.getStr(table, "@for");
                        String title = MapUtil.getStr(table, "title");
                        // 根据table的@for的值在map中取出对应的表头
                        Map<String, String> headerMap = map.get(forKey);
                        ExcelHeaderConfig headerConfig = headerConfigs.get(forKey);

                        if (headerMap != null) {
                            // 添加标题行
                            if (title != null && !title.trim().isEmpty()) {
                                org.example.inspection.utils.ExcelUtil.addTitleRow(writer, title.trim(), currentRow, 0, headerMap.size() - 1);
                                currentRow++; // 标题行占用一行
                            }

                            // 设置表头别名
                            org.example.inspection.utils.ExcelUtil.setHeaderAlias(writer, headerMap);
                            // 设置表头样式
                            org.example.inspection.utils.ExcelUtil.setHeaderStyle(writer);
                            
                            // 设置列宽
                            if (headerConfig != null) {
                                org.example.inspection.utils.ExcelUtil.setColumnWidths(writer, headerConfig);
                            }

                            // 数据在json中取出
                            // 注意：这里应该使用table的@for属性值来获取数据
                            Object tableData = json.get(MapUtil.getStr(table, "@for"));
                            // 即使没有数据也要写入表头
                            if (tableData instanceof List) {
                                List<?> dataList = (List<?>) tableData;
                                // 确保数据按照headerMap的字段顺序显示
                                List<Map<String, Object>> orderedDataList = new ArrayList<>();
                                Object thresholdObj = ((Map<String, Object>) dataList.get(0)).get("threshold");
                                for (Object dataItem : dataList) {
                                    Map<String, Object> orderedItem = new LinkedHashMap<>();
                                    if (dataItem instanceof Map) {
                                        // 严格按照headerMap的顺序和字段来生成表头和数据
                                        for (String field : headerMap.keySet()) {
                                            orderedItem.put(field.toLowerCase(), ((Map<String, Object>) dataItem).get(field.toLowerCase()));
                                        }
                                    } else {
                                        // 如果不是Map类型，创建一个只包含headerMap字段的空对象
                                        for (String field : headerMap.keySet()) {
                                            orderedItem.put(field.toLowerCase(), "");
                                        }
                                    }
                                    orderedDataList.add(orderedItem);
                                }
                                // 使用passRows确保数据写入到正确的位置
                                writer.passRows(currentRow);
                                writer.write(orderedDataList, true);
                                
                                // Apply threshold highlighting for each data row
                                for (int rowIndex = 0; rowIndex < orderedDataList.size(); rowIndex++) {
                                    Map<String, Object> rowData = orderedDataList.get(rowIndex);
                                    int actualRow = currentRow + rowIndex + 1; // +1 for header row
                                    
                                    // Check each field against its threshold
                                    int colIndex = 0;
                                    for (String fieldName : headerMap.keySet()) {
                                        org.example.inspection.utils.ExcelUtil.checkThresholdAndHighlight(
                                            writer, rowData, thresholdObj, fieldName, colIndex, actualRow);
                                        colIndex++;
                                    }
                                }
                                
                                currentRow += orderedDataList.size() + 1; // +1 for header
                            } else if (tableData != null) {
                                List<Map<String, Object>> dataList = new ArrayList<>();
                                // 确保数据按照headerMap的字段顺序显示
                                if (tableData instanceof Map) {
                                    Map<String, Object> orderedItem = new LinkedHashMap<>();
                                    // 严格按照headerMap的顺序和字段来生成表头和数据
                                    for (String field : headerMap.keySet()) {
                                        orderedItem.put(field.toLowerCase(), ((Map<String, Object>) tableData).get(field.toLowerCase()));
                                    }
                                    dataList.add(orderedItem);
                                } else {
                                    // 如果不是Map类型，创建一个只包含headerMap字段的空对象
                                    Map<String, Object> orderedItem = new LinkedHashMap<>();
                                    for (String field : headerMap.keySet()) {
                                        orderedItem.put(field.toLowerCase(), "");
                                    }
                                    dataList.add(orderedItem);
                                }
                                // 使用passRows确保数据写入到正确的位置
                                writer.passRows(1);
                                writer.write(dataList, true);
                                currentRow += 2; // Single object + header
                            } else {
                                // 即使data为null也要写入表头
                                // 创建一个包含所有表头字段的空对象
                                Map<String, Object> emptyRow = new LinkedHashMap<>();
                                for (String field : headerMap.keySet()) {
                                    emptyRow.put(field.toLowerCase(), "");
                                }
                                List<Map<String, Object>> emptyList = new ArrayList<>();
                                emptyList.add(emptyRow);
                                // 使用passRows确保数据写入到正确的位置
                                writer.passRows(1);
                                writer.write(emptyList, true);
                                currentRow += 2; // Only header
                            }
                        }
                        isFirstTable = false;
                    }
                }

            }

            // Set response headers
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // Write to response output stream
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
            writer.close();
            out.flush();
            IoUtil.close(out);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error generating Excel: " + e.getMessage());
        }
    }

    @GetMapping("/test-text")
    public String testText() {
        System.out.println("Testing text endpoint");
        return "Hello, this is a test!";
    }
}