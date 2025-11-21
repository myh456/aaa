package org.example.inspection.utils;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Excel表头配置类，用于存储列宽和合并单元格信息
 */
public class ExcelHeaderConfig {
    // 存储每列的宽度配置，key为列索引，value为宽度值
    private Map<Integer, Integer> columnWidths = new HashMap<>();
    
    // 存储合并单元格信息，每个元素包含开始列、结束列和合并后的标题
    private List<MergedCellInfo> mergedCells = new ArrayList<>();
    
    // 存储每个字段的列索引
    private Map<String, Integer> fieldIndices = new HashMap<>();
    
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
     * 获取所有列宽配置
     * @return 列宽配置映射
     */
    public Map<Integer, Integer> getColumnWidths() {
        return columnWidths;
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
     * 获取所有合并单元格信息
     * @return 合并单元格信息列表
     */
    public List<MergedCellInfo> getMergedCells() {
        return mergedCells;
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
     * 获取所有字段索引映射
     * @return 字段索引映射
     */
    public Map<String, Integer> getFieldIndices() {
        return fieldIndices;
    }
    
    /**
     * 合并单元格信息类
     */
    public static class MergedCellInfo {
        private int startCol;
        private int endCol;
        private String title;
        
        public MergedCellInfo(int startCol, int endCol, String title) {
            this.startCol = startCol;
            this.endCol = endCol;
            this.title = title;
        }
        
        public int getStartCol() {
            return startCol;
        }
        
        public int getEndCol() {
            return endCol;
        }
        
        public String getTitle() {
            return title;
        }
    }
}