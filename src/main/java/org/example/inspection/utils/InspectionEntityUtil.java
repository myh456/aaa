package org.example.inspection.utils;

import lombok.Getter;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 巡检配置工具类
 * 用于加工并获取和巡检相关所有配置
 */
@Component
@SuppressWarnings("unchecked")
public class InspectionEntityUtil {
    @Autowired
    private XmlParser xmlParser;
    @Value("${config.path}")
    private String configPath;

    @Getter
    private Map<String, Object> cmds;
    @Getter
    private Map<String, Object> sqls;
    @Getter
    private Map<String, Map<Integer, Object>> rules;
    @Getter
    private Map<Integer, Object> charas;
    @Getter
    private Map<String, Object> defaultConfig;
    @Getter
    private Map<String, Object> defaultCheck;
    @Getter
    private List<Object> servers;
    @Getter
    private List<Object> databases;
    @Getter
    private List<Object> excelConfigs;

    @PostConstruct
    public void init() throws DocumentException {
        initConfig();
        initServer();
        initDatabase();
        initExcelConfig();
    }

    public void initConfig() throws DocumentException {
        // 初始化命令配置
        cmds = new HashMap<>();
        sqls = new HashMap<>();
        Map<String, Object> runs = xmlParser.parseXml(configPath + "system/ServersRunning.xml");
        runs.forEach((k, v) -> {
            if (v instanceof Map) {
                if (((Map<String, Object>) v).containsKey("cmd")) {
                    cmds.put(k, v);
                } else if (((Map<String, Object>) v).containsKey("sql")) {
                    sqls.put(k, v);
                }
            } else {
                if (((Map<String, Object>) ((List<Object>) v).get(0)).containsKey("cmd")) {
                    cmds.put(k, v);
                } else if (((Map<String, Object>) ((List<Object>) v).get(0)).containsKey("sql")) {
                    sqls.put(k, v);
                }
            }
        });
        // 初始化巡检规则配置
        Map<String, Object> rulesMap = xmlParser.parseXml(configPath + "RulesConfig.xml");
        rules = new HashMap<>();
        rulesMap.forEach((k, v) -> {
            rules.put(k, new HashMap<>());
            if (v instanceof List) {
                for (Object v1 : (List<Object>) v) {
                    rules.get(k).put(Integer.parseInt(((Map<String, Object>) v1).get("@id").toString()), v1);
                }
            } else {
                rules.get(k).put(Integer.parseInt(((Map<String, Object>) v).get("@id").toString()), v);
            }
        });
        // 初始化角色配置
        Map<String, Object> charasMap = xmlParser.parseXml(configPath + "CharacterConfig.xml");
        charas = new HashMap<>();
        charasMap.forEach((k, v) -> {
            List<Object> charaList;
            if (v instanceof Map) {
                charaList = Collections.singletonList(v);
            } else {
                charaList = (List<Object>) v;
            }
            for (Object c : charaList) {
                Map<String, Object> chara = (Map<String, Object>) c;
                ((Map<String, Object>) c).forEach((k1, v1) -> {
                    if (k1.startsWith("@")) {
                        return;
                    }
                    if (v1 instanceof Map && ((Map<String, Object>) v1).containsKey("@id")) {
                        Integer id = Integer.parseInt(((Map<String, Object>) v1).get("@id").toString());
                        chara.put(k1, rules.get(k1).get(id));
                    }
                });
                charas.put(Integer.parseInt(chara.get("@id").toString()), chara);
            }
        });
        // 默认配置
        Map<String, Object> defaultMap = xmlParser.parseXml(configPath + "DefaultConfig.xml");
        defaultCheck = new HashMap<>();
        defaultConfig = defaultMap;
        Map<String, Object> checks = (Map<String, Object>) defaultMap.get("check");
        defaultConfig.remove("check");
        for (String k : checks.keySet()) {
            if (((Map<String, Object>) checks.get(k)).containsKey("@id")) {
                Map<String, Object> item = (Map<String, Object>) rules.get(k).get(Integer.parseInt(((Map<String, Object>) checks.get(k)).get("@id").toString()));
                defaultCheck.put(k, item);
            } else {
                defaultCheck.put(k, checks.get(k));
            }
        }
    }

    public void initServer() throws DocumentException {
        Map<String, Object> serversMap = xmlParser.parseXml(configPath + "ServersConfig.xml");
        servers = new ArrayList<>();
        List<Object> serverList;
        if (serversMap.get("server") instanceof Map) {
            serverList = Collections.singletonList(serversMap.get("server"));
        } else {
            serverList = (List<Object>) serversMap.get("server");
        }
        for (Object item : serverList) {
            Map<String, Object> check = (Map<String, Object>) item;
            Map<String, Object> server = new HashMap<>();
            boolean isDefault = true;
            for (String k : check.keySet()) {
                if (k.startsWith("@")) {
                    server.put(k, check.get(k));
                }
                if (!k.equals("character")) {
                    continue;
                }
                List<Object> charaList;
                if (check.get(k) instanceof Map) {
                    charaList = Collections.singletonList(check.get(k));
                } else {
                    charaList = (List<Object>) check.get(k);
                }
                for (Object ch : charaList) {
                    int id = Integer.parseInt(((Map<String, Object>) ch).get("@id").toString());
                    server.putAll((Map<String, Object>) charas.get(id));
                    isDefault = false;
                }
            }
            if (isDefault) {
                defaultCheck.forEach((k, v) -> {
                    if (((Map<String, Object>) v).containsKey("cmd")) {
                        server.put(k, v);
                    }
                });
            }
            // 将命令插入服务器配置
            server.forEach((k, v) -> {
                if (k.startsWith("@")) {
                    return;
                }
                if (cmds.containsKey(k)) {
                    // 保存服务器配置的变量
                    Map<String, List<String>> serverVars = new HashMap<>();
                    List<Map<String, String>> serverMultiVars = new ArrayList<>();
                    ((Map<String, Object>) v).forEach((k1, v1) -> {
                        if (k1.endsWith("-attr")) {
                            String attr = k1.substring(0, k1.length() - 5);
                            if (v1 instanceof List) {
                                serverVars.put(attr, (List<String>) v1);
                            } else {
                                serverVars.put(attr, Collections.singletonList(v1.toString()));
                            }
                        } else if ("attrs".equals(k1)) {
                            if (v1 instanceof List) {
                                serverMultiVars.addAll((List<Map<String, String>>) v1);
                            } else {
                                serverMultiVars.add((Map<String, String>) v1);
                            }
                        }
                    });
                    // 获取并插入命令
                    Object cmd = cmds.get(k);
                    if (cmd instanceof Map) {
                        ((Map<String, Object>) v).putAll((Map<String, Object>) cmd);
                    } else {
                        for (Object c : (List<Object>) cmd) {
                            if (((Map<String, Object>) c).get("@os").equals(server.get("@os"))) {
                                ((Map<String, Object>) v).putAll((Map<String, Object>) c);
                                break;
                            }
                        }
                    }
                    // 根据变量生成cmd数组
                    if (!serverVars.isEmpty()) {
                        cmd = ((Map<String, Object>) v).get("cmd").toString();
                        List<String> cmds = new ArrayList<>();
                        for (String var : serverVars.keySet()) {
                            if (cmd.toString().contains(String.format("{{%s}}", var))) {
                                for (String val : serverVars.get(var)) {
                                    cmds.add(cmd.toString().replace(String.format("{{%s}}", var), val));
                                }
                            }
                        }
                        ((Map<String, Object>) v).put("cmd", cmds);
                    } else if (!serverMultiVars.isEmpty()) {
                        cmd = ((Map<String, Object>) v).get("cmd").toString();
                        List<String> cmds = new ArrayList<>();
                        for (Map<String, String> vars : serverMultiVars) {
                            String cmdTemp = cmd.toString();
                            for (Map.Entry<String, String> varsEntry : vars.entrySet()) {
                                String var = varsEntry.getKey();
                                String val = varsEntry.getValue();
                                if (cmd.toString().contains(String.format("{{%s}}", var))) {
                                    cmdTemp = cmdTemp.replace(String.format("{{%s}}", var), val);
                                }
                            }
                            cmds.add(cmdTemp);
                        }
                        ((Map<String, Object>) v).put("cmd", cmds);
                    }
                }
            });
            servers.add(server);
        }
    }

    public void initDatabase() throws DocumentException {
        Map<String, Object> databaseMap = xmlParser.parseXml(configPath + "DatabaseConfig.xml");
        databases = new ArrayList<>();
        List<Object> databaseList;
        if (databaseMap.get("database") instanceof Map) {
            databaseList = Collections.singletonList(databaseMap.get("database"));
        } else {
            databaseList = (List<Object>) databaseMap.get("database");
        }
        for (Object item : databaseList) {
            Map<String, Object> check = (Map<String, Object>) item;
            Map<String, Object> database = new HashMap<>();
            boolean isDefault = true;
            for (String k : check.keySet()) {
                if (k.startsWith("@")) {
                    database.put(k, check.get(k));
                }
                if (!k.equals("character")) {
                    continue;
                }
                List<Object> charaList;
                if (check.get(k) instanceof Map) {
                    charaList = Collections.singletonList(check.get(k));
                } else {
                    charaList = (List<Object>) check.get(k);
                }
                for (Object ch : charaList) {
                    int id = Integer.parseInt(((Map<String, Object>) ch).get("@id").toString());
                    database.putAll((Map<String, Object>) charas.get(id));
                    isDefault = false;
                }
            }
            if (isDefault) {
                defaultCheck.forEach((k, v) -> {
                    if (((Map<String, Object>) v).containsKey("sql")) {
                        database.put(k, v);
                    }
                });
            }
            // 将sql插入数据库配置
            Map<String, Object> databaseTemp = new HashMap<>(database);
            databaseTemp.forEach((k, v) -> {
                if (k.startsWith("@")) {
                    return;
                }
                List<Object> sqlList;
                if (v instanceof Map) {
                    sqlList = Collections.singletonList(v);
                } else {
                    sqlList = new ArrayList<>((List<Object>) v);
                }
                for (Object sql : sqlList) {
                    if (sqls.containsKey(k)) {
                        // 保存数据库配置的变量
                        Map<String, List<String>> databaseVars = new HashMap<>();
                        ((Map<String, Object>) v).forEach((k1, v1) -> {
                            if (k1.endsWith("-attr")) {
                                String attr = k1.substring(0, k1.length() - 5);
                                if (v1 instanceof List) {
                                    databaseVars.put(attr, (List<String>) v1);
                                } else {
                                    databaseVars.put(attr, Collections.singletonList(v1.toString()));
                                }
                            }
                        });
                        // 获取并插入sql
                        sql = sqls.get(k);
                        if (sql instanceof Map) {
                            ((Map<String, Object>) v).putAll((Map<String, Object>) sql);
                        } else {
                            for (Object s : (List<Object>) sql) {
                                if (((Map<String, Object>) s).get("@name").equals(database.get("@name"))) {
                                    ((Map<String, Object>) v).putAll((Map<String, Object>) s);
                                    break;
                                }
                            }
                        }
                        // 根据变量生成sql数组
                        if (!databaseVars.isEmpty()) {
                            sql = ((Map<String, Object>) v).get("sql").toString();
                            List<String> sqls = new ArrayList<>();
                            for (String var : databaseVars.keySet()) {
                                if (sql.toString().contains(String.format("{{%s}}", var))) {
                                    for (String val : databaseVars.get(var)) {
                                        sqls.add(sql.toString().replace(String.format("{{%s}}", var), val));
                                    }
                                }
                            }
                            ((Map<String, Object>) v).put("sql", sqls);
                        }
                    }
                }
            });
            databases.add(database);
        }
    }


    /**
     * 初始化Excel配置
     *
     * @throws DocumentException 如果解析XML文件时发生错误
     */
    private void initExcelConfig() throws DocumentException {
        Map<String, Object> excelSheetMap = xmlParser.parseXml(configPath + "system/ExcelSheet.xml");
        this.excelConfigs = (List<Object>) excelSheetMap.get("sheet");
    }

    /**
     * 从配置中提取Excel表头
     *
     * @return 一个映射，其中键是表标识符，值是字段映射
     * 对于单个表，键是"for"属性值
     * 对于多个表，键是"service#" + "for"属性值
     */
    public Map<String, Map<String, String>> extractExcelHeaders() {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        if (excelConfigs == null) {
            return result;
        }
        for (Object sheetObj : excelConfigs) {
            if (!(sheetObj instanceof Map)) {
                continue;
            }
            Map<String, Object> sheet = (Map<String, Object>) sheetObj;
            Object tableObj = sheet.get("table");
            if (tableObj instanceof Map) {
                // 单表情况
                processTable(result, (Map<String, Object>) tableObj, false);
            } else if (tableObj instanceof List) {
                // 多表情况
                List<Map<String, Object>> tables = (List<Map<String, Object>>) tableObj;
                for (Map<String, Object> table : tables) {
                    processTable(result, table, true);
                }
            }
        }
        // 向所有标题添加通用字段
        for (Map<String, String> headerMap : result.values()) {
            headerMap.put("checker", "巡检人");
            headerMap.put("date", "巡检日期");
            headerMap.put("time", "巡检时间");
        }

        return result;
    }

    /**
     * 从配置中提取Excel表头及列宽配置
     *
     * @return 一个映射，其中键是表标识符，值是ExcelHeaderConfig对象
     */
    public Map<String, ExcelHeaderConfig> extractExcelHeaderConfigs() {
        Map<String, ExcelHeaderConfig> result = new LinkedHashMap<>();
        if (excelConfigs == null) {
            return result;
        }
        for (Object sheetObj : excelConfigs) {
            if (!(sheetObj instanceof Map)) {
                continue;
            }
            Map<String, Object> sheet = (Map<String, Object>) sheetObj;
            Object tableObj = sheet.get("table");
            if (tableObj instanceof Map) {
                // 单表情况
                processTableWithConfig(result, (Map<String, Object>) tableObj, false);
            } else if (tableObj instanceof List) {
                // 多表情况
                List<Map<String, Object>> tables = (List<Map<String, Object>>) tableObj;
                for (Map<String, Object> table : tables) {
                    processTableWithConfig(result, table, true);
                }
            }
        }
        return result;
    }

    /**
     * 处理表元素以提取表头映射
     *
     * @param result     要填充的结果映射
     * @param table      要处理的表元素
     * @param isMultiple 是否是包含多个表的sheet
     */
    private void processTable(Map<String, Map<String, String>> result, Map<String, Object> table, boolean isMultiple) {
        String forKey = (String) table.get("@for");
        Map<String, Object> row = extractTableRow(forKey, table);
        if (row == null) {
            return;
        }
        Map<String, String> headerMap = new LinkedHashMap<>();
        Object cellObj = row.get("cell");
        // 统一处理单元格，无论是一个还是多个
        processCells(headerMap, cellObj);
        // 根据是单表还是多表情况确定键
        // String key = isMultiple ? "service#" + forKey : forKey;
        result.put(forKey, headerMap);
    }

    /**
     * 处理表元素以提取表头配置
     *
     * @param result     要填充的结果映射
     * @param table      要处理的表元素
     * @param isMultiple 是否是包含多个表的sheet
     */
    private void processTableWithConfig(Map<String, ExcelHeaderConfig> result, Map<String, Object> table, boolean isMultiple) {
        String forKey = (String) table.get("@for");
        Map<String, Object> row = extractTableRow(forKey, table);
        if (row == null) {
            return;
        }
        ExcelHeaderConfig headerConfig = new ExcelHeaderConfig();
        // 计算表头的最大深度
        int maxDepth = calculateMaxDepth(row.get("cell"));
        headerConfig.setHeaderRowCount(maxDepth);

        // 处理通用字段的列索引，紧跟在配置字段后面
        int fieldCount = calculateFieldCount(row.get("cell"));
        headerConfig.setFieldIndex("checker", fieldCount);
        headerConfig.setFieldIndex("date", fieldCount + 1);
        headerConfig.setFieldIndex("time", fieldCount + 2);
        // 设置通用字段的宽度为20
        headerConfig.setColumnWidth(fieldCount, 20);
        headerConfig.setColumnWidth(fieldCount + 1, 20);
        headerConfig.setColumnWidth(fieldCount + 2, 20);
        Object cellObj = row.get("cell");
        // 统一处理单元格，无论是一个还是多个
        processCellsWithConfig(headerConfig, cellObj, new int[]{0}, 0, maxDepth); // 使用数组来跟踪列索引
        result.put(forKey, headerConfig);
    }

    /**
     * 提取表的行数据
     *
     * @param forKey 表key
     * @param table  表元素
     * @return 行数据，如果表无效则返回null
     */
    private Map<String, Object> extractTableRow(String forKey, Map<String, Object> table) {
        if (forKey == null) {
            return null;
        }
        Object rowObj = table.get("row");
        if (!(rowObj instanceof Map)) {
            return null;
        }
        return (Map<String, Object>) rowObj;
    }

    /**
     * 计算单元格的最大嵌套深度
     *
     * @param cellObj 单元格对象
     * @return 最大深度
     */
    private int calculateMaxDepth(Object cellObj) {
        if (cellObj == null) {
            return 1;
        }
        int maxDepth = 1;
        if (cellObj instanceof List) {
            List<Map<String, Object>> cells = (List<Map<String, Object>>) cellObj;
            for (Map<String, Object> cell : cells) {
                Object nestedCellObj = cell.get("cell");
                if (nestedCellObj != null) {
                    maxDepth = Math.max(maxDepth, 1 + calculateMaxDepth(nestedCellObj));
                }
            }
        } else if (cellObj instanceof Map) {
            Map<String, Object> cell = (Map<String, Object>) cellObj;
            Object nestedCellObj = cell.get("cell");
            if (nestedCellObj != null) {
                maxDepth = Math.max(maxDepth, 1 + calculateMaxDepth(nestedCellObj));
            }
        }
        return maxDepth;
    }

    /**
     * 计算字段数量（不包括嵌套字段）
     *
     * @param cellObj 单元格对象
     * @return 字段数量
     */
    private int calculateFieldCount(Object cellObj) {
        if (cellObj == null) {
            return 0;
        }
        int count = 0;
        if (cellObj instanceof List) {
            List<Map<String, Object>> cells = (List<Map<String, Object>>) cellObj;
            for (Map<String, Object> cell : cells) {
                Object nestedCellObj = cell.get("cell");
                if (nestedCellObj != null) {
                    count += calculateFieldCount(nestedCellObj);
                } else {
                    // 没有嵌套单元格，这是一个字段
                    count++;
                }
            }
        } else if (cellObj instanceof Map) {
            Map<String, Object> cell = (Map<String, Object>) cellObj;
            Object nestedCellObj = cell.get("cell");
            if (nestedCellObj != null) {
                count += calculateFieldCount(nestedCellObj);
            } else {
                // 没有嵌套单元格，这是一个字段
                count++;
            }
        }
        return count;
    }

    /**
     * 处理单元格列表或单个单元格（带配置）
     *
     * @param headerConfig 要填充的表头配置
     * @param cellObj      单元格对象或单元格列表
     * @param columnIndex  列索引计数器（使用数组以便在递归中修改）
     * @param currentDepth 当前深度
     * @param maxDepth     最大深度
     */
    private void processCellsWithConfig(ExcelHeaderConfig headerConfig, Object cellObj, int[] columnIndex, int currentDepth, int maxDepth) {
        if (cellObj instanceof List) {
            // 多个单元格
            List<Map<String, Object>> cells = (List<Map<String, Object>>) cellObj;
            for (Map<String, Object> cell : cells) {
                processCellWithConfig(headerConfig, cell, columnIndex, currentDepth, maxDepth);
            }
        } else if (cellObj instanceof Map) {
            // 单个单元格
            processCellWithConfig(headerConfig, (Map<String, Object>) cellObj, columnIndex, currentDepth, maxDepth);
        }
    }

    /**
     * 处理单元格元素以提取字段配置
     *
     * @param headerConfig 要填充的表头配置
     * @param cell         要处理的单元格元素
     * @param columnIndex  列索引计数器
     * @param currentDepth 当前深度
     * @param maxDepth     最大深度
     */
    private void processCellWithConfig(ExcelHeaderConfig headerConfig, Map<String, Object> cell, int[] columnIndex, int currentDepth, int maxDepth) {
        String from = (String) cell.get("@from");
        String name = (String) cell.get("@name");
        // 处理嵌套单元格（合并单元格情况）
        Object nestedCellObj = cell.get("cell");
        if (nestedCellObj != null) {
            // 有子单元格的情况
            int startCol = columnIndex[0];
            // 计算子单元格数量
            int childCount = getChildCount(nestedCellObj);
            // 如果当前是表头的第一行，需要合并单元格（水平合并）
            if (currentDepth == 0 && name != null && childCount > 1) {
                // 在第一行添加合并单元格 (仅当子节点数量大于1时才合并)
                headerConfig.addMergedCell(0, 0, startCol, startCol + childCount - 1, name);
            }
            // 处理嵌套的单元格
            processCellsWithConfig(headerConfig, nestedCellObj, columnIndex, currentDepth + 1, maxDepth);
        } else {
            // 没有子单元格的情况
            if (from != null && name != null) {
                headerConfig.setFieldIndex(from, columnIndex[0]);
                // 如果表头有多行，需要纵向合并（仅当跨越多行时才合并）
                if (maxDepth > 1 && currentDepth < maxDepth - 1) {
                    headerConfig.addMergedCell(currentDepth, maxDepth - 1, columnIndex[0], columnIndex[0], name);
                }
                // 处理列宽
                Object widthObj = cell.get("@width");
                if (widthObj != null) {
                    try {
                        int width = Integer.parseInt(widthObj.toString());
                        headerConfig.setColumnWidth(columnIndex[0], width);
                    } catch (NumberFormatException e) {
                        // 忽略无效的宽度值
                    }
                }
                columnIndex[0]++;
            }
        }
    }

    /**
     * 获取子单元格数量
     *
     * @param cellObj 单元格对象
     * @return 子单元格数量
     */
    private int getChildCount(Object cellObj) {
        if (cellObj instanceof List) {
            return ((List<?>) cellObj).size();
        } else if (cellObj instanceof Map) {
            return 1;
        }
        return 0;
    }

    /**
     * 处理单元格列表或单个单元格
     *
     * @param headerMap 要填充的表头映射
     * @param cellObj   单元格对象或单元格列表
     */
    private void processCells(Map<String, String> headerMap, Object cellObj) {
        if (cellObj instanceof List) {
            // 多个单元格
            List<Map<String, Object>> cells = (List<Map<String, Object>>) cellObj;
            for (Map<String, Object> cell : cells) {
                processCell(headerMap, cell);
            }
        } else if (cellObj instanceof Map) {
            // 单个单元格
            processCell(headerMap, (Map<String, Object>) cellObj);
        }
    }

    /**
     * 处理单元格元素以提取字段映射
     *
     * @param headerMap 要填充的表头映射
     * @param cell      要处理的单元格元素
     */
    private void processCell(Map<String, String> headerMap, Map<String, Object> cell) {
        String from = (String) cell.get("@from");
        String name = (String) cell.get("@name");
        if (from != null && name != null) {
            headerMap.put(from, name);
        }
        // 处理嵌套单元格
        Object nestedCellObj = cell.get("cell");
        processCells(headerMap, nestedCellObj);
    }
}