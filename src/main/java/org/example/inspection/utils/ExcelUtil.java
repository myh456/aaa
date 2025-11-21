package org.example.inspection.utils;

import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.StyleSet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class ExcelUtil {

    public static ExcelWriter setHeaderAlias(ExcelWriter excelWriter, Map<String, String> headerMap) {
        excelWriter.clearHeaderAlias();
        Set<String> headerKeys = headerMap.keySet();
        for (String headerKey : headerKeys) {
            excelWriter.addHeaderAlias(headerKey.toLowerCase(), headerMap.get(headerKey));
        }
        // 只写出加了别名的字段，未添加别名的属性不会写出
        excelWriter.setOnlyAlias(true);
        return excelWriter;
    }

    public static int getHeaderIndex(Map<String, String> headerMap, String key) {
        Set<String> headerKeys = headerMap.keySet();
        LinkedList<String> list = new LinkedList<>(headerKeys);
        return list.indexOf(key);
    }

    public static ExcelWriter setHeaderStyle(ExcelWriter excelWriter) {
        StyleSet styleSet = excelWriter.getStyleSet();
        CellStyle headerStyle = styleSet.getHeadCellStyle();
        // 设置字体颜色
        Font font = excelWriter.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        headerStyle.setFont(font);
        // 设置背景色
        headerStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置自动换行
        headerStyle.setWrapText(true);
        return excelWriter;
    }


    public static ExcelWriter setCellBackgroundColor(ExcelWriter excelWriter, int x, int y, short colorIndex) {
        // Create cell style
        CellStyle cellStyle = excelWriter.createCellStyle(x, y);
        // Set cell background color
        cellStyle.setFillForegroundColor(colorIndex);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // Set cell alignment to center
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // Set top border width and color
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        // Set right border width and color
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        // Set bottom border width and color
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        // Set left border width and color
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        return excelWriter;
    }

    /**
     * Check if a value meets threshold criteria and highlight the cell if needed
     *
     * @param excelWriter Excel writer instance
     * @param rowData Map containing row data
     * @param fieldName Name of the field to check
     * @param x Column index
     * @param y Row index
     * @return ExcelWriter with updated cell style if threshold condition is met
     */
    public static ExcelWriter checkThresholdAndHighlight(ExcelWriter excelWriter, Map<String, Object> rowData, Object thresholdObj, String fieldName, int x, int y) {
        // Get threshold data from row data
        if (!(thresholdObj instanceof Map)) {
            return excelWriter;
        }

        Map<String, Object> thresholdMap = (Map<String, Object>) thresholdObj;

        // Check if there's a threshold configuration for this field
        if (!thresholdMap.containsKey(fieldName)) {
            return excelWriter;
        }

        Object fieldThreshold = thresholdMap.get(fieldName);
        if (!(fieldThreshold instanceof Map)) {
            return excelWriter;
        }

        Map<String, Object> fieldThresholdMap = (Map<String, Object>) fieldThreshold;

        // Get the actual value for comparison
        Object fieldValueObj = rowData.get(fieldName);
        if (fieldValueObj == null) {
            return excelWriter;
        }

        try {
            double fieldValue = Double.parseDouble(String.valueOf(fieldValueObj));

            // Check for higher threshold
            if (fieldThresholdMap.containsKey("higher")) {
                double higherThreshold = Double.parseDouble(String.valueOf(fieldThresholdMap.get("higher")));
                if (fieldValue >= higherThreshold) {
                    // Highlight with red color if value is greater than or equal to higher threshold
                    setCellBackgroundColor(excelWriter, x, y, IndexedColors.RED.getIndex());
                }
            }

            // Check for lower threshold
            if (fieldThresholdMap.containsKey("lower")) {
                double lowerThreshold = Double.parseDouble(String.valueOf(fieldThresholdMap.get("lower")));
                if (fieldValue <= lowerThreshold) {
                    // Highlight with red color if value is less than or equal to lower threshold
                    setCellBackgroundColor(excelWriter, x, y, IndexedColors.RED.getIndex());
                }
            }
        } catch (NumberFormatException e) {
            // If value cannot be parsed to double, do nothing
            return excelWriter;
        }

        return excelWriter;
    }

    /**
     * 在指定行添加标题并合并单元格
     *
     * @param excelWriter Excel写入器
     * @param title       标题内容
     * @param startRow    开始行
     * @param startCol    开始列
     * @param endCol      结束列
     * @return Excel写入器
     */
    public static ExcelWriter addTitleRow(ExcelWriter excelWriter, String title, int startRow, int startCol, int endCol) {
        // 在指定位置写入标题
        excelWriter.writeCellValue(startCol, startRow, title);

        // 合并单元格
        if (startCol < endCol) {
            Sheet sheet = excelWriter.getSheet();
            CellRangeAddress region = new CellRangeAddress(startRow, startRow, startCol, endCol);
            sheet.addMergedRegion(region);

            // 为合并的单元格设置样式
            CellStyle titleStyle = excelWriter.createCellStyle();
            Font font = excelWriter.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 14);
            titleStyle.setFont(font);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 应用样式到所有合并的单元格
            for (int col = startCol; col <= endCol; col++) {
                excelWriter.getOrCreateCell(col, startRow).setCellStyle(titleStyle);
            }
        }

        return excelWriter;
    }
    
    /**
     * 设置列宽
     * @param excelWriter Excel写入器
     * @param headerConfig 表头配置
     * @return Excel写入器
     */
    public static ExcelWriter setColumnWidths(ExcelWriter excelWriter, ExcelHeaderConfig headerConfig) {
        Sheet sheet = excelWriter.getSheet();
        for (Map.Entry<Integer, Integer> entry : headerConfig.getColumnWidths().entrySet()) {
            int columnIndex = entry.getKey();
            int width = entry.getValue();
            // POI中的列宽单位是1/256个字符宽度
            sheet.setColumnWidth(columnIndex, width * 256);
        }
        return excelWriter;
    }
    
    /**
     * 设置合并单元格
     * @param excelWriter Excel写入器
     * @param headerConfig 表头配置
     * @param rowIndex 表头行索引
     * @return Excel写入器
     */
    public static ExcelWriter setMergedCells(ExcelWriter excelWriter, ExcelHeaderConfig headerConfig, int rowIndex) {
        Sheet sheet = excelWriter.getSheet();
        for (ExcelHeaderConfig.MergedCellInfo mergedCell : headerConfig.getMergedCells()) {
            int startCol = mergedCell.getStartCol();
            int endCol = mergedCell.getEndCol();
            String title = mergedCell.getTitle();
            
            // 写入合并单元格的标题
            excelWriter.writeCellValue(startCol, rowIndex, title);
            
            // 合并单元格
            CellRangeAddress region = new CellRangeAddress(rowIndex, rowIndex, startCol, endCol);
            sheet.addMergedRegion(region);
            
            // 为合并的单元格设置样式
            CellStyle headerStyle = excelWriter.getStyleSet().getHeadCellStyle();
            for (int col = startCol; col <= endCol; col++) {
                Cell cell = excelWriter.getOrCreateCell(col, rowIndex);
                cell.setCellStyle(headerStyle);
            }
        }
        return excelWriter;
    }
}