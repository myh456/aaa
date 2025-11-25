package org.example.inspection.utils;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WordConverter {
    @Value("${path.word}")
    private String wordPath;

    // 字号对照表 (磅值 pt)
    private final int FONT_SIZE_H1 = 22; // 二号
    private final int FONT_SIZE_H2 = 15; // 小三
    private final int FONT_SIZE_BODY = 14; // 四号
    private final String FONT_FAMILY = "宋体";

    public void convertFromMd(String mdContent) {
        try (XWPFDocument document = new XWPFDocument()) {

            // 按行分割字符串
            String[] lines = mdContent.split("\n");

            for (String line : lines) {
                // 去除首尾空白，但保留中间空格
                String text = line.trim();

                if (text.isEmpty()) {
                    // 如果需要保留空行，可以创建一个空段落，这里选择跳过
                    continue;
                }

                if (text.startsWith("# ")) {
                    // 处理一级标题
                    createH1(document, text.substring(2).trim());
                } else if (text.startsWith("## ")) {
                    // 处理二级标题
                    createH2(document, text.substring(3).trim());
                } else {
                    // 处理正文
                    createBodyParagraph(document, text);
                }
            }
            String outputPath = wordPath + "output.docx";
            File file = new File(outputPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file = new File(outputPath);
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile();

            // 写入文件
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                document.write(out);
                System.out.println("文件生成成功: " + outputPath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建一级标题: 居中、加粗、二号
     */
    private void createH1(XWPFDocument doc, String text) {
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER); // 居中

        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(true); // 加粗
        run.setFontSize(FONT_SIZE_H1); // 二号
        run.setFontFamily(FONT_FAMILY);
    }

    /**
     * 创建二级标题: 居左、加粗、小三
     */
    private void createH2(XWPFDocument doc, String text) {
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT); // 居左

        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(true); // 加粗
        run.setFontSize(FONT_SIZE_H2); // 小三
        run.setFontFamily(FONT_FAMILY);
    }

    /**
     * 创建正文: 两端对齐、四号、支持局部加粗
     */
    private void createBodyParagraph(XWPFDocument doc, String text) {
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.BOTH); // 两端对齐
        paragraph.setIndentationFirstLine(560); // 首行缩进2字符

        // 使用正则匹配 **bold**
        Pattern boldPattern = Pattern.compile("\\*\\*(.+?)\\*\\*");
        Matcher matcher = boldPattern.matcher(text);

        int lastIndex = 0;

        while (matcher.find()) {
            // 1. 添加 ** 之前的普通文本
            String plainText = text.substring(lastIndex, matcher.start());
            if (!plainText.isEmpty()) {
                appendRun(paragraph, plainText, false);
            }

            // 2. 添加 ** 包裹的加粗文本
            String boldText = matcher.group(1);
            if (!boldText.isEmpty()) {
                appendRun(paragraph, boldText, true);
            }

            lastIndex = matcher.end();
        }

        // 3. 添加剩余的普通文本
        if (lastIndex < text.length()) {
            String remainingText = text.substring(lastIndex);
            appendRun(paragraph, remainingText, false);
        }
    }

    /**
     * 辅助方法：向段落添加文本 Run
     */
    private void appendRun(XWPFParagraph paragraph, String text, boolean isBold) {
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(FONT_SIZE_BODY); // 四号
        run.setFontFamily(FONT_FAMILY);
        run.setBold(isBold);
    }
}
