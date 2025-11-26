package org.example.inspection.utils;

import lombok.Getter;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WordTextUtil {
    @Value("${path.config}")
    private String configPath;
    @Autowired
    private WordConverter wordConverter;
    @Getter
    private Map<String, String> summary;

    private Document document;
    private Map<String, Map<String, String>> extract;

    // 用于匹配 {{key}} 的正则
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.*?)}}");

    @PostConstruct
    public void init() throws DocumentException {
        String xmlPath = configPath + "/system/WordText.xml";
        SAXReader reader = new SAXReader();
        InputStream is;
        try {
            if (xmlPath.startsWith("classpath:"))
                is = this.getClass().getClassLoader().getResourceAsStream(xmlPath.substring(10));
            else
                is = Files.newInputStream(Paths.get(xmlPath));
        } catch (Exception e) {
            System.err.println("Error opening file: " + xmlPath);
            return;
        }
        document = reader.read(is);
        extract = new HashMap<>();
        Element element = document.getRootElement();
        List<Element> children = element.elements();
        children.forEach(elem -> {
            try {
                String tagName = elem.getName();
                if ("section".equals(tagName)) {
                    Element ext = elem.element("extract");
                    String key = ext.attributeValue("key");
                    String val = ext.attributeValue("val");
                    Map<String, String> e = new HashMap<>();
                    e.put("key", key);
                    e.put("val", val);
                    extract.put(elem.attributeValue("for"), e);
                }
            } catch (Exception ignored) {}
        });
    }

    public Map<String, Map<String, String>> getExtract() {
        return extract;
    }

    /**
     * 用于将巡检结果转word
     * @param data 巡检结果
     */
    public Map<String, String> parse(Map<String, Object> data, String fileName) {
        Element root = document.getRootElement();
        StringBuilder sb = new StringBuilder();
        processNodes(root.content(), data, sb);
        String md = sb.toString().trim();
        parseMd(md);
        wordConverter.convertFromMd(md, fileName);
        return summary;
    }

    private void parseMd(String md) {
        summary = new HashMap<>();
        String[] lines = md.split(System.lineSeparator());
        for(int i = 1; i < lines.length; i += 2) {
            summary.put(lines[i].substring(3), lines[i + 1].replaceAll("\\*\\*", ""));
        }
    }

    /**
     * 核心递归方法：处理节点列表
     */
    private static void processNodes(List<Node> nodes, Map<String, Object> currentData, StringBuilder sb) {
        for (Node node : nodes) {
            if (node instanceof Text) {
                // 处理文本节点（替换占位符 + 简单的空白清理）
                String text = node.getText();
                text = resolvePlaceholders(text, currentData);
                text = text.replaceAll("[\\n\\r]+\\s*", "");
                sb.append(text);

            } else if (node instanceof Element) {
                Element element = (Element) node;
                String tagName = element.getName();

                switch (tagName) {
                    case "extract":
                        break;
                    case "title":
                        sb.append("# ").append(element.getTextTrim()).append(System.lineSeparator());
                        break;
                    case "section":
                        handleSection(element, currentData, sb);
                        break;
                    case "subtitle":
                        sb.append("## ").append(element.getTextTrim()).append(System.lineSeparator());
                        break;
                    case "text":
                        // text 标签本身不输出内容，只作为容器，继续递归其子节点
                        processNodes(element.content(), currentData, sb);
                        sb.append(System.lineSeparator()); // text块结束后通常换行
                        break;
                    case "if":
                        handleIf(element, currentData, sb);
                        break;
                    case "foreach":
                        handleForeach(element, currentData, sb);
                        break;
                    default:
                        // 对于未知的标签，默认递归处理其子节点
                        processNodes(element.content(), currentData, sb);
                }
            }
        }
    }

    /**
     * 处理 Section 标签：切换数据上下文
     */
    @SuppressWarnings("unchecked")
    private static void handleSection(Element element, Map<String, Object> rootData, StringBuilder sb) {
        String forKey = element.attributeValue("for");
        Object subData = rootData.get(forKey);

        if (subData instanceof Map) {
            processNodes(element.content(), (Map<String, Object>) subData, sb);
        }
    }

    /**
     * 处理 If 标签
     */
    private static void handleIf(Element element, Map<String, Object> data, StringBuilder sb) {
        String notZeroKey = element.attributeValue("notzero");
        String zeroKey = element.attributeValue("zero");

        boolean conditionMet = false;

        if (notZeroKey != null) {
            conditionMet = getInt(data, notZeroKey) != 0;
        } else if (zeroKey != null) {
            conditionMet = getInt(data, zeroKey) == 0;
        }

        if (conditionMet) {
            processNodes(element.content(), data, sb);
        }
    }

    /**
     * 处理 Foreach 标签
     */
    @SuppressWarnings("unchecked")
    private static void handleForeach(Element element, Map<String, Object> data, StringBuilder sb) {
        String fromKey = element.attributeValue("from");
        String begin = element.attributeValue("begin", "");
        String end = element.attributeValue("end", "，");
        String separator = element.attributeValue("separator", "、");

        Object listObj = data.get(fromKey);
        if (listObj instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) listObj;

            if (!list.isEmpty()) {
                sb.append(begin);
                for (int i = 0; i < list.size(); i++) {
                    Map<String, Object> itemMap = list.get(i);

                    // 构建临时的 item 上下文，注入 key 和 val
                    // 根据样例，failed list 是 [{"ip": val}, {"ip2": val}] 这种单Entry的Map结构
                    if (itemMap != null && !itemMap.isEmpty()) {
                        Map.Entry<String, Object> entry = itemMap.entrySet().iterator().next();

                        // 使用装饰器或者临时Map，为了不污染原数据，这里简单地复用 data 但覆盖 key/val
                        // 注意：实际生产建议新建一个 Map putAll(data) 然后 put key/val
                        // 这里为了简便直接创建一个包含 data 和当前 item 的混合视图
                        DataScope scope = new DataScope(data);
                        scope.put("key", entry.getKey());
                        scope.put("val", entry.getValue());

                        processNodes(element.content(), scope, sb);

                        // 添加分隔符（如果不是最后一个）
                        if (i < list.size() - 1) {
                            sb.append(separator);
                        }
                    }
                }
                sb.append(end);
            }
        }
    }

    /**
     * 替换字符串中的 {{key}}
     */
    private static String resolvePlaceholders(String text, Map<String, Object> data) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Object val = data.get(key);
            matcher.appendReplacement(sb, val != null ? val.toString() : "");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static int getInt(Map<String, Object> data, String key) {
        Object obj = data.get(key);
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 简单的 Map 包装类，用于 Foreach 内部产生临时作用域
     * 优先读取临时变量(key, val)，没有则读取父级数据
     */
    static class DataScope implements Map<String, Object> {
        private final Map<String, Object> parent;
        private final java.util.HashMap<String, Object> current = new java.util.HashMap<>();

        public DataScope(Map<String, Object> parent) { this.parent = parent; }

        @Override
        public Object get(Object key) {
            if (current.containsKey(key)) return current.get(key);
            return parent.get(key);
        }

        @Override public Object put(String key, Object value) { return current.put(key, value); }
        @Override public int size() { return 0; }
        @Override public boolean isEmpty() { return false; }
        @Override public boolean containsKey(Object key) { return current.containsKey(key) || parent.containsKey(key); }
        @Override public boolean containsValue(Object value) { return false; }
        @Override public Object remove(Object key) { return null; }
        @Override public void putAll(Map<? extends String, ?> m) {}
        @Override public void clear() {}
        @Override public Set<String> keySet() { return null; }
        @Override public java.util.Collection<Object> values() { return null; }
        @Override public Set<Entry<String, Object>> entrySet() { return null; }
    }
}