package org.example.inspection.utils;

import cn.hutool.poi.excel.ExcelWriter;
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

    /**
     * 设置单元格背景颜色和边框样式
     *
     * @param excelWriter Excel写入器
     * @param x           单元格列索引
     * @param y           单元格行索引
     * @param colorIndex  颜色索引
     * @return Excel写入器
     */
    public static ExcelWriter setCellBackgroundColor(ExcelWriter excelWriter, int x, int y, short colorIndex) {
        // 创建单元格样式
        CellStyle cellStyle = excelWriter.createCellStyle(x, y);
        // 设置单元格背景颜色
        cellStyle.setFillForegroundColor(colorIndex);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置单元格居中对齐
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 设置上边框宽度和颜色
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        // 设置右边框宽度和颜色
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        // 设置下边框宽度和颜色
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        // 设置左边框宽度和颜色
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        return excelWriter;
    }

    /**
     * 检查值是否符合阈值条件，如果需要则突出显示单元格
     *
     * @param excelWriter Excel写入器实例
     * @param rowData     包含行数据的映射
     * @param fieldName   字段名称
     * @param x           列索引
     * @param y           行索引
     * @return 如果该字段超出阈值则返回true，否则返回false
     */
    public static boolean checkThresholdAndHighlight(ExcelWriter excelWriter, Map<String, Object> rowData, Object thresholdObj, String fieldName, int x, int y) {
        // 从行数据中获取阈值数据
        if (!(thresholdObj instanceof Map)) {
            return false;
        }

        Map<String, Object> thresholdMap = (Map<String, Object>) thresholdObj;

        // 检查此字段是否有阈值配置
        if (!thresholdMap.containsKey(fieldName)) {
            return false;
        }

        Object fieldThreshold = thresholdMap.get(fieldName);
        if (!(fieldThreshold instanceof Map)) {
            return false;
        }

        Map<String, Object> fieldThresholdMap = (Map<String, Object>) fieldThreshold;

        // 获取实际值进行比较
        Object fieldValueObj = rowData.get(fieldName);
        if (fieldValueObj == null) {
            return false;
        }

        try {
            double fieldValue = Double.parseDouble(String.valueOf(fieldValueObj));
            boolean isExceeded = false;

            // 检查较高阈值
            if (fieldThresholdMap.containsKey("higher")) {
                double higherThreshold = Double.parseDouble(String.valueOf(fieldThresholdMap.get("higher")));
                if (fieldValue >= higherThreshold) {
                    // 如果值大于或等于较高阈值，则用红色突出显示
                    setCellBackgroundColor(excelWriter, x, y, IndexedColors.RED.getIndex());
                    isExceeded = true;
                }
            }

            // 检查较低阈值
            if (fieldThresholdMap.containsKey("lower")) {
                double lowerThreshold = Double.parseDouble(String.valueOf(fieldThresholdMap.get("lower")));
                if (fieldValue <= lowerThreshold) {
                    // 如果值小于或等于较低阈值，则用红色突出显示
                    setCellBackgroundColor(excelWriter, x, y, IndexedColors.RED.getIndex());
                    isExceeded = true;
                }
            }
            
            return isExceeded;
        } catch (NumberFormatException e) {
            // 如果值无法解析为double，则不执行任何操作
            return false;
        }
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
            CellStyle titleStyle = createTitleCellStyle(excelWriter);

            // 应用样式到所有合并的单元格
            for (int col = startCol; col <= endCol; col++) {
                excelWriter.getOrCreateCell(col, startRow).setCellStyle(titleStyle);
            }
        }

        return excelWriter;
    }

    /**
     * 设置列宽
     *
     * @param excelWriter  Excel写入器
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

        // 设置通用字段的列宽为20
        Map<String, Integer> fieldIndices = headerConfig.getFieldIndices();
        String[] universalFields = {"checker", "date", "time"};
        for (String field : universalFields) {
            if (fieldIndices.containsKey(field)) {
                int columnIndex = fieldIndices.get(field);
                sheet.setColumnWidth(columnIndex, 20 * 256);
            }
        }

        return excelWriter;
    }

    /**
     * 设置合并单元格
     *
     * @param excelWriter  Excel写入器
     * @param headerConfig 表头配置
     * @param rowIndex     表头行索引
     * @return Excel写入器
     */
    public static ExcelWriter setMergedCells(ExcelWriter excelWriter, ExcelHeaderConfig headerConfig, int rowIndex) {
        Sheet sheet = excelWriter.getSheet();
        for (ExcelHeaderConfig.MergedCellInfo mergedCell : headerConfig.getMergedCells()) {
            int startCol = mergedCell.getStartCol();
            int endCol = mergedCell.getEndCol();
            int startRow = mergedCell.getStartRow() + rowIndex;
            int endRow = mergedCell.getEndRow() + rowIndex;
            String title = mergedCell.getTitle();

            // 写入合并单元格的标题
            excelWriter.writeCellValue(startCol, startRow, title);

            // 只有当区域包含多个单元格时才合并
            if (startRow != endRow || startCol != endCol) {
                // 合并单元格
                CellRangeAddress region = new CellRangeAddress(startRow, endRow, startCol, endCol);
                sheet.addMergedRegion(region);

                // 为合并的单元格设置样式
                CellStyle headerStyle = createHeaderCellStyle(excelWriter);
                for (int row = startRow; row <= endRow; row++) {
                    for (int col = startCol; col <= endCol; col++) {
                        Cell cell = excelWriter.getOrCreateCell(col, row);
                        cell.setCellStyle(headerStyle);
                    }
                }
            } else {
                // 单个单元格只需设置样式
                Cell cell = excelWriter.getOrCreateCell(startCol, startRow);
                CellStyle headerStyle = createHeaderCellStyle(excelWriter);
                cell.setCellStyle(headerStyle);
                cell.setCellStyle(headerStyle);
            }
        }
        // 处理通用字段的纵向合并
        Map<String, Integer> fieldIndices = headerConfig.getFieldIndices();
        int headerRowCount = headerConfig.getHeaderRowCount();
        // 如果表头有多行，需要纵向合并通用字段
        if (headerRowCount > 1) {
            String[] universalFields = {"checker", "date", "time"};
            for (String field : universalFields) {
                if (fieldIndices.containsKey(field)) {
                    int colIndex = fieldIndices.get(field);
                    CellRangeAddress region = new CellRangeAddress(rowIndex, rowIndex + headerRowCount - 1, colIndex, colIndex);
                    sheet.addMergedRegion(region);

                    // 为合并的单元格设置样式
                    CellStyle headerStyle = createHeaderCellStyle(excelWriter);
                    for (int row = rowIndex; row <= rowIndex + headerRowCount - 1; row++) {
                        Cell cell = excelWriter.getOrCreateCell(colIndex, row);
                        cell.setCellStyle(headerStyle);
                    }
                }
            }
        }
        return excelWriter;
    }

    /**
     * 创建多级表头
     *
     * @param excelWriter  Excel写入器
     * @param headerConfig 表头配置
     * @param headerMap    表头映射
     * @param rowIndex     起始行索引
     * @return Excel写入器
     */
    public static ExcelWriter createMultiLevelHeader(ExcelWriter excelWriter, ExcelHeaderConfig headerConfig, Map<String, String> headerMap, int rowIndex) {
        Sheet sheet = excelWriter.getSheet();
        int headerRowCount = headerConfig.getHeaderRowCount();

        // 先写入所有字段名到对应的行和列
        Map<String, Integer> fieldIndices = headerConfig.getFieldIndices();
        CellStyle headerStyle = excelWriter.getStyleSet().getHeadCellStyle();

        // 计算通用字段的列索引（巡检人、巡检日期、巡检时间）
        int universalFieldStartIndex = 0;
        if (!fieldIndices.isEmpty()) {
            for (int index : fieldIndices.values()) {
                if (index > universalFieldStartIndex) {
                    universalFieldStartIndex = index;
                }
            }
            universalFieldStartIndex++; // 从最后一个字段之后开始放置通用字段
        }

        // 处理合并单元格
        setMergedCells(excelWriter, headerConfig, rowIndex);

        // 写入普通字段名到对应的行和列（只写入非通用字段）
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            String fieldKey = entry.getKey();
            String fieldName = entry.getValue();

            // 跳过通用字段，因为它们已经在合并单元格处理中处理过了
            if ("checker".equals(fieldKey) || "date".equals(fieldKey) || "time".equals(fieldKey)) {
                continue;
            }
            if (fieldIndices.containsKey(fieldKey)) {
                int colIndex = fieldIndices.get(fieldKey);
                int rowOffset = headerRowCount - 1; // 字段名通常在最后一行

                // 写入字段名
                excelWriter.writeCellValue(colIndex, rowIndex + rowOffset, fieldName);

                // 设置单元格样式 (仅当没有在合并单元格中设置时)
                Cell cell = excelWriter.getOrCreateCell(colIndex, rowIndex + rowOffset);
                CellStyle cellStyle = createHeaderCellStyle(excelWriter);
                cell.setCellStyle(cellStyle);
            }
        }

        // 处理通用字段（只写入字段名，不处理合并，因为已在setMergedCells中处理）
        String[] universalFields = {"checker", "date", "time"};
        String[] universalFieldNames = {"巡检人", "巡检日期", "巡检时间"};
        for (int i = 0; i < universalFields.length; i++) {
            String fieldKey = universalFields[i];
            String fieldName = universalFieldNames[i];
            if (fieldIndices.containsKey(fieldKey)) {
                int colIndex = fieldIndices.get(fieldKey);
                // 写入字段名（始终写在第一行）
                excelWriter.writeCellValue(colIndex, rowIndex, fieldName);
                // 设置单元格样式
                Cell cell = excelWriter.getOrCreateCell(colIndex, rowIndex);
                CellStyle cellStyle = createHeaderCellStyle(excelWriter);
                cell.setCellStyle(cellStyle);
            }
        }
        // 设置列宽
        setColumnWidths(excelWriter, headerConfig);
        return excelWriter;
    }

    /**
     * 创建表头单元格样式
     *
     * @param excelWriter Excel写入器
     * @return 表头单元格样式
     */
    private static CellStyle createHeaderCellStyle(ExcelWriter excelWriter) {
        CellStyle headerStyle = excelWriter.createCellStyle();
        Font font = excelWriter.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置边框
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        return headerStyle;
    }

    /**
     * 创建标题单元格样式
     *
     * @param excelWriter Excel写入器
     * @return 标题单元格样式
     */
    private static CellStyle createTitleCellStyle(ExcelWriter excelWriter) {
        CellStyle titleStyle = excelWriter.createCellStyle();
        Font font = excelWriter.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        titleStyle.setFont(font);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置边框
        titleStyle.setBorderTop(BorderStyle.THIN);
        titleStyle.setBorderRight(BorderStyle.THIN);
        titleStyle.setBorderBottom(BorderStyle.THIN);
        titleStyle.setBorderLeft(BorderStyle.THIN);
        return titleStyle;
    }
}