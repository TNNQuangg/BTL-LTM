package com.nhom8.common.dto;

import java.util.List;

public class StrokeDTO {
    public static final String TOOL_BRUSH = "BRUSH";
    public static final String TOOL_ERASER = "ERASER";
    public static final String TOOL_CLEAR = "CLEAR";

    private String tool;
    private String color;
    private double width;
    private List<Double> xPoints;
    private List<Double> yPoints;

    public StrokeDTO() {}

    public StrokeDTO(String tool, String color, double width, List<Double> xPoints, List<Double> yPoints) {
        this.tool = tool;
        this.color = color;
        this.width = width;
        this.xPoints = xPoints;
        this.yPoints = yPoints;
    }

    public String getTool() { return tool; }
    public void setTool(String tool) { this.tool = tool; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public List<Double> getxPoints() { return xPoints; }
    public void setxPoints(List<Double> xPoints) { this.xPoints = xPoints; }

    public List<Double> getyPoints() { return yPoints; }
    public void setyPoints(List<Double> yPoints) { this.yPoints = yPoints; }

    public boolean isClear() {
        return TOOL_CLEAR.equals(tool);
    }

    public boolean isUndo() {
        return "UNDO".equals(tool);
    }
}
