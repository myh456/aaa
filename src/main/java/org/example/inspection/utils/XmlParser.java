package org.example.inspection.utils;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
@SuppressWarnings("unchecked")
public class XmlParser {
    public static Object parseElement(Element element) {
        Map<String, Object> map = new HashMap<>();

        // 1. 解析子元素
        List<Element> children = element.elements();
        if (!children.isEmpty()) {
            for (Element child : children) {
                String tagName = child.getName();
                // 递归解析子元素
                Object childValue = parseElement(child);

                if (map.containsKey(tagName)) {
                    // 检查是否已存在同名元素，如果存在，则转换为 List
                    Object existing = map.get(tagName);
                    if (existing instanceof List) {
                        ((List<Object>) existing).add(childValue);
                    } else {
                        List<Object> list = new ArrayList<>();
                        list.add(existing);
                        list.add(childValue);
                        map.put(tagName, list);
                    }
                } else {
                    // 没有同名元素
                    map.put(tagName, childValue);
                }
            }
        }

        // 2. 处理标签的属性和文本内容
        Iterator<Attribute> attrIterator = element.attributeIterator();
        while (attrIterator.hasNext()) {
            Attribute attr = attrIterator.next();
            map.put("@" + attr.getName(), attr.getValue());
        }

        // 获取标签的文本内容
        String text = element.getTextTrim();
        return map.isEmpty() ? text : map;
    }
    public Map<String, Object> parseXml(String xmlPath) throws DocumentException {
        SAXReader reader = new SAXReader();
        InputStream is;
        try {
            if (xmlPath.startsWith("classpath:"))
                is = this.getClass().getClassLoader().getResourceAsStream(xmlPath.substring(10));
            else
                is = Files.newInputStream(Paths.get(xmlPath));
        } catch (Exception e) {
            System.err.println("Error opening file: " + xmlPath);
            return null;
        }
        Document doc = reader.read(is);
        return (Map<String, Object>) parseElement(doc.getRootElement());
    }
}
