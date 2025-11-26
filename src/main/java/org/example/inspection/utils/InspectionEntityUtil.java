package org.example.inspection.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 巡检配置工具类
 * 用于加工并获取和巡检相关所有配置
 */
@Component
public class InspectionEntityUtil {
    @Autowired
    private XmlParser xmlParser;
    @Value("${path.config}")
    private String configPath;
    private String currentOS;

    @Getter
    private Map<String, Object> cmds;
    @Getter
    private Map<String, Object> sqls;
    @Getter
    private Map<String, Object> shells;
    @Getter
    private Map<String, Map<Integer, Object>> rules;
    @Getter
    private Map<Integer, Object> charas;
    @Getter
    private Map<String, Object> defaultConfig;
    @Getter
    private Map<String, Object> defaultServerCheck;
    @Getter
    private Map<String, Object> defaultDatabaseCheck;
    @Getter
    private Map<String, Object> defaultRemoteCheck;
    @Getter
    private List<Map<String, Object>> servers;
    @Getter
    private List<Map<String, Object>> databases;
    @Getter
    private List<Map<String, Object>> remotes;
    @Getter
    private List<Object> excelConfigs;

    @PostConstruct
    public void init() throws DocumentException {
        currentOS = System.getProperty("os.name");
        if(currentOS.toLowerCase().contains("win")) {
            currentOS = "win";
        } else {
            currentOS = "linux";
        }
        initConfig();
        initServer();
        initDatabase();
        initRemote();
        initExcelConfig();
    }

    @SuppressWarnings("unchecked")
    public void initConfig() throws DocumentException {
        // 初始化命令配置
        cmds = new HashMap<>();
        sqls = new HashMap<>();
        shells = new HashMap<>();
        Map<String, Object> runs = xmlParser.parseXml(configPath + "system/ServersRunning.xml");
        runs.forEach((k, v) -> {
            if (v instanceof Map) {
                if (((Map<String, Object>) v).containsKey("cmd")) {
                    cmds.put(k, v);
                } else if (((Map<String, Object>) v).containsKey("sql")) {
                    sqls.put(k, v);
                } else if (((Map<String, Object>) v).containsKey("shell")) {
                    shells.put(k, v);
                }
            } else {
                if (((Map<String, Object>) ((List<Object>) v).get(0)).containsKey("cmd")) {
                    cmds.put(k, v);
                } else if (((Map<String, Object>) ((List<Object>) v).get(0)).containsKey("sql")) {
                    sqls.put(k, v);
                } else if (((Map<String, Object>) ((List<Object>) v).get(0)).containsKey("shell")) {
                    shells.put(k, v);
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
        defaultServerCheck = new HashMap<>();
        defaultDatabaseCheck = new HashMap<>();
        defaultRemoteCheck = new HashMap<>();
        defaultConfig = defaultMap;
        if (defaultMap.containsKey("server-check") && !"".equals(defaultMap.get("server-check"))) {
            Map<String, Object> checks = (Map<String, Object>) defaultMap.get("server-check");
            defaultConfig.remove("server-check");
            for (String k : checks.keySet()) {
                if (((Map<String, Object>) checks.get(k)).containsKey("@id")) {
                    Map<String, Object> item = (Map<String, Object>) rules.get(k).get(Integer.parseInt(((Map<String, Object>) checks.get(k)).get("@id").toString()));
                    defaultServerCheck.put(k, item);
                } else {
                    defaultServerCheck.put(k, checks.get(k));
                }
            }
        }
        if (defaultMap.containsKey("database-check") && !"".equals(defaultMap.get("database-check"))) {
            Map<String, Object> checks = (Map<String, Object>) defaultMap.get("database-check");
            defaultConfig.remove("database-check");
            for (String k : checks.keySet()) {
                if (((Map<String, Object>) checks.get(k)).containsKey("@id")) {
                    Map<String, Object> item = (Map<String, Object>) rules.get(k).get(Integer.parseInt(((Map<String, Object>) checks.get(k)).get("@id").toString()));
                    defaultDatabaseCheck.put(k, item);
                } else {
                    defaultDatabaseCheck.put(k, checks.get(k));
                }
            }
        }
        if (defaultMap.containsKey("remote-check") && !"".equals(defaultMap.get("remote-check"))) {
            Map<String, Object> checks = (Map<String, Object>) defaultMap.get("remote-check");
            defaultConfig.remove("remote-check");
            for (String k : checks.keySet()) {
                if (((Map<String, Object>) checks.get(k)).containsKey("@id")) {
                    Map<String, Object> item = (Map<String, Object>) rules.get(k).get(Integer.parseInt(((Map<String, Object>) checks.get(k)).get("@id").toString()));
                    defaultRemoteCheck.put(k, item);
                } else {
                    defaultRemoteCheck.put(k, checks.get(k));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadConfiguration(String xmlPath, String rootKey, List<Map<String, Object>> targetList,
                                   Map<String, Object> templatesMap, Map<String, Object> defaultCheck, String cmdKey) throws DocumentException {
        Map<String, Object> xmlMap = xmlParser.parseXml(xmlPath);
        List<Object> itemList = getAsList(xmlMap.get(rootKey));

        for (Object item : itemList) {
            Map<String, Object> rawConfig = (Map<String, Object>) item;
            Map<String, Object> finalConfig = new HashMap<>();
            boolean isDefault = true;

            // 1. 基础属性复制与角色(character)继承
            for (String k : rawConfig.keySet()) {
                if (k.startsWith("@")) {
                    finalConfig.put(k, rawConfig.get(k));
                }
                if ("character".equals(k)) {
                    List<Object> charaList = getAsList(rawConfig.get(k));
                    for (Object ch : charaList) {
                        int id = Integer.parseInt(((Map<String, Object>) ch).get("@id").toString());
                        // 使用了JSON序列化进行深拷贝。
                        Map<String, Object> chara = deepCopyMap(charas.get(id));
                        finalConfig.putAll(chara);
                        isDefault = false;
                    }
                }
            }

            // 2. 默认配置处理
            if (isDefault) {
                finalConfig.putAll(defaultCheck);
            }

            // 3. 提取当前对象的上下文属性 (用于过滤和变量替换)
            Map<String, Object> contextAttrs = new HashMap<>();
            contextAttrs.put("@currentOS", currentOS);
            finalConfig.forEach((k, v) -> {
                if (k.startsWith("@")) contextAttrs.put(k, v);
            });

            // 4. 处理具体的检查项 (命令/SQL注入与变量替换)
            // 使用临时Map避免遍历时修改异常，或者直接遍历Key
            Map<String, Object> configCopy = new HashMap<>(finalConfig);
            configCopy.forEach((k, v) -> {
                if (k.startsWith("@")) return;

                // 检查是否在模版源(cmds/sqls)中定义了该项
                if (templatesMap.containsKey(k)) {
                    processConfigItem(k, (Map<String, Object>) v, templatesMap.get(k), contextAttrs, cmdKey);
                }
            });

            // 将处理后的 configCopy 重新赋值给 finalConfig (因为 processConfigItem 是引用修改)
            // 实际上 finalConfig 中的 value 对象引用和 configCopy 是一样的，所以直接修改 v 即可生效

            targetList.add(finalConfig);
        }
    }

    /**
     * 处理单个配置项：解析变量、合并模版、生成最终命令
     */
    @SuppressWarnings("unchecked")
    private void processConfigItem(String key, Map<String, Object> itemValue, Object templateObj,
                                   Map<String, Object> contextAttrs, String cmdKey) {

        // A. 提取局部变量配置
        Map<String, List<String>> singleVars = new HashMap<>();
        List<Map<String, String>> multiVars = new ArrayList<>();

        itemValue.forEach((k1, v1) -> {
            if (k1.endsWith("-attr")) {
                String attr = k1.substring(0, k1.length() - 5);
                singleVars.put(attr, getAsStringList(v1));
            } else if ("attrs".equals(k1)) {
                List<Object> rawList = getAsList(v1);
                rawList.forEach(item -> {
                    if (item instanceof Map) {
                        multiVars.add((Map<String, String>) item);
                    }
                });
            }
        });

        // B. 合并模版内容
        if (templateObj instanceof Map) {
            itemValue.putAll((Map<String, Object>) templateObj);
        } else if (templateObj instanceof List) {
            // 根据属性过滤 List 类型的模版
            Map<String, Object> matchedTemplate = ((List<Map<String, Object>>) templateObj).stream()
                    .filter(c -> contextAttrs.entrySet().stream()
                            .allMatch(attr -> !c.containsKey(attr.getKey()) ||
                                    Objects.equals(c.get(attr.getKey()), attr.getValue())))
                    .findFirst().orElse(null);

            if (matchedTemplate == null) return;
            itemValue.putAll(matchedTemplate);

            // 将上下文属性反向补全到 itemValue
            contextAttrs.forEach((ka, va) -> itemValue.putIfAbsent(ka, va));
        }

        // C. 生成最终命令 (变量替换)
        Object rawCmd = itemValue.get(cmdKey);
        if (rawCmd == null) return;
        String cmdTemplate = rawCmd.toString();

        if (!singleVars.isEmpty()) {
            List<String> resultCmds = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : singleVars.entrySet()) {
                String varKey = "{{" + entry.getKey() + "}}";
                if (cmdTemplate.contains(varKey)) {
                    for (String val : entry.getValue()) {
                        resultCmds.add(cmdTemplate.replace(varKey, val));
                    }
                }
            }
            if(!resultCmds.isEmpty()) itemValue.put(cmdKey, resultCmds);

        } else if (!multiVars.isEmpty()) {
            List<String> resultCmds = new ArrayList<>();
            for (Map<String, String> vars : multiVars) {
                String temp = cmdTemplate;
                for (Map.Entry<String, String> entry : vars.entrySet()) {
                    temp = temp.replace("{{" + entry.getKey() + "}}", entry.getValue());
                }
                resultCmds.add(temp);
            }
            itemValue.put(cmdKey, resultCmds);

        } else {
            // 尝试使用 contextAttrs 自动替换
            String temp = cmdTemplate;
            boolean modified = false;
            for (String var : contextAttrs.keySet()) {
                String target = "{{" + var.substring(1) + "}}";
                if (temp.contains(target)) {
                    temp = temp.replace(target, contextAttrs.get(var).toString());
                    modified = true;
                }
            }
            if (modified) itemValue.put(cmdKey, temp);
        }
    }

    // --- 辅助工具方法 ---

    // 统一处理 Object 转 List (处理单个对象和List对象的差异)
    @SuppressWarnings("unchecked")
    private List<Object> getAsList(Object obj) {
        if (obj == null) return Collections.emptyList();
        if (obj instanceof List) return (List<Object>) obj;
        return new ArrayList<>(Collections.singletonList(obj));
    }

    // 统一处理 Object 转 List<String>
    @SuppressWarnings("unchecked")
    private List<String> getAsStringList(Object obj) {
        if (obj instanceof List) return (List<String>) obj;
        return Collections.singletonList(obj.toString());
    }

    // 深拷贝 (保留原逻辑中的 JSON 方式，虽然性能一般但最安全)
    private Map<String, Object> deepCopyMap(Object source) {
        return JSONObject.parseObject(JSON.toJSONString(source));
    }

    public void initServer() throws DocumentException {
        this.servers = new ArrayList<>();
        loadConfiguration(configPath + "ServersConfig.xml", "server", this.servers, this.cmds, this.defaultServerCheck, "cmd");
    }

    public void initDatabase() throws DocumentException {
        this.databases = new ArrayList<>();
        loadConfiguration(configPath + "DatabaseConfig.xml", "database", this.databases, this.sqls, this.defaultDatabaseCheck, "sql");
    }

    public void initRemote() throws DocumentException {
        this.remotes = new ArrayList<>();
        loadConfiguration(configPath + "RemoteConfig.xml", "remote", this.remotes, this.shells, this.defaultRemoteCheck, "shell");
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