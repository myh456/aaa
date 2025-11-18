package org.example.inspection.utils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NetworkCheckTool {
    // 服务器信息类：存储name、url和statusCode
    static class ServerInfo {
        String name;      // 服务器名称
        String url;       // 服务器地址
        String statusCode; // 服务器状态码

        ServerInfo(String name, String url) {
            this.name = name;
            this.url = url;
            this.statusCode = ""; // 初始化为空字符串
        }

        // 设置服务器状态码的方法
        public void setStatusCode(String statusCode) {
            this.statusCode = statusCode;
        }
    }

    private List<ServerInfo> serverList = new ArrayList<>();

    public NetworkCheckTool() {
        // 加载配置文件（仅包含name和url）
        loadServerConfig("config/ConnectivityConfig.xml");
        // 检查所有服务器并更新状态码
        checkAllServers();
    }

    // 加载服务器配置（从XML文件读取name和url）
    private void loadServerConfig(String configPath) {
        try {
            SAXReader reader = new SAXReader();
            InputStream is = getClass().getClassLoader().getResourceAsStream(configPath);

            if (is == null) {
                System.err.println("配置文件不存在: " + configPath);
                return;
            }

            Document document = reader.read(is);
            Element root = document.getRootElement();

            // 解析所有服务器的name和url，创建ServerInfo对象（暂不设置statusCode）
            List<Element> serverElements = root.elements("server");
            for (Element serverElem : serverElements) {
                String name = serverElem.elementText("name");
                String url = serverElem.elementText("url");
                serverList.add(new ServerInfo(name, url));
            }

        } catch (DocumentException e) {
            System.err.println("解析配置文件错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 检查所有服务器连通性，更新状态码并输出结果
    private void checkAllServers() {
        for (ServerInfo server : serverList) {
            String status = checkServerStatus(server.url);
            // 将状态码存入ServerInfo对象
            server.setStatusCode(status);
            System.out.printf("%s (%s): %s%n", server.name, server.url, server.statusCode);
        }
    }

    // 检查单个服务器的状态，返回字符串类型的状态描述
    private String checkServerStatus(String url) {
        try {
            URL serverUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int statusCode = connection.getResponseCode();
            connection.disconnect();
            return String.valueOf(statusCode); // 正常状态：返回"200"

        } catch (Exception e) {
            // 异常状态
            return "连接失败: " + e.getMessage();
        }
    }
}