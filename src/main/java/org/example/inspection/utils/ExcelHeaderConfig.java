package org.example.inspection.utils;

import lombok.Getter;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Excel表头配置类，用于存储列宽和合并单元格信息
 * @author CUI
 */
@Getter
public class ExcelHeaderConfig {
    /**
     * -- GETTER --
     *  获取所有列宽配置
     *
     * @return 列宽配置映射
     */
    // 存储每列的宽度配置，key为列索引，value为宽度值
    private Map<Integer, Integer> columnWidths = new HashMap<>();

    /**
     * -- GETTER --
     *  获取所有合并单元格信息
     *
     * @return 合并单元格信息列表
     */
    // 存储合并单元格信息，每个元素包含开始列、结束列和合并后的标题
    private List<MergedCellInfo> mergedCells = new ArrayList<>();

    /**
     * -- GETTER --
     *  获取所有字段索引映射
     *
     * @return 字段索引映射
     */
    // 存储每个字段的列索引
    private Map<String, Integer> fieldIndices = new HashMap<>();

    /**
     * -- GETTER --
     *  获取表头行数
     *
     * @return 表头行数
     */
    // 表头总行数
    private int headerRowCount = 1;
    
    /**
     * 设置列宽
     * @param columnIndex 列索引
     * @param width 宽度值
     */
    public void setColumnWidth(int columnIndex, int width) {
        columnWidths.put(columnIndex, width);
    }
    
    /**
     * 获取列宽
     * @param columnIndex 列索引
     * @return 宽度值，如果没有设置则返回默认值-1
     */
    public int getColumnWidth(int columnIndex) {
        return columnWidths.getOrDefault(columnIndex, -1);
    }

    /**
     * 添加合并单元格信息
     * @param startCol 开始列索引
     * @param endCol 结束列索引
     * @param title 合并后的标题
     */
    public void addMergedCell(int startCol, int endCol, String title) {
        mergedCells.add(new MergedCellInfo(startCol, endCol, title));
    }
    
    /**
     * 添加合并单元格信息（指定行列范围）
     * @param startRow 开始行索引
     * @param endRow 结束行索引
     * @param startCol 开始列索引
     * @param endCol 结束列索引
     * @param title 合并后的标题
     */
    public void addMergedCell(int startRow, int endRow, int startCol, int endCol, String title) {
        mergedCells.add(new MergedCellInfo(startRow, endRow, startCol, endCol, title));
    }

    /**
     * 设置字段列索引
     * @param fieldName 字段名
     * @param columnIndex 列索引
     */
    public void setFieldIndex(String fieldName, int columnIndex) {
        fieldIndices.put(fieldName, columnIndex);
    }
    
    /**
     * 获取字段列索引
     * @param fieldName 字段名
     * @return 列索引
     */
    public int getFieldIndex(String fieldName) {
        return fieldIndices.getOrDefault(fieldName, -1);
    }

    /**
     * 设置表头行数
     * @param rowCount 行数
     */
    public void setHeaderRowCount(int rowCount) {
        this.headerRowCount = rowCount;
    }

    /**
     * 合并单元格信息类
     */
    @Getter
    public static class MergedCellInfo {
        private int startCol;
        private int endCol;
        private String title;
        private int startRow;
        private int endRow;
        
        public MergedCellInfo(int startCol, int endCol, String title) {
            this.startCol = startCol;
            this.endCol = endCol;
            this.title = title;
            this.startRow = 0;
            this.endRow = 0;
        }
        
        public MergedCellInfo(int startRow, int endRow, int startCol, int endCol, String title) {
            this.startRow = startRow;
            this.endRow = endRow;
            this.startCol = startCol;
            this.endCol = endCol;
            this.title = title;
        }

    }
}