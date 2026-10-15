package com.nhom8.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StrokeDTO {
    public static final String TOOL_BRUSH = "BRUSH";
    public static final String TOOL_ERASER = "ERASER";
    public static final String TOOL_CLEAR = "CLEAR";

    private String tool;
    private String color;
    private double width;

    @JsonProperty("xPoints")
    private List<Double> xPoints;

    @JsonProperty("yPoints")
    private List<Double> yPoints;

    private Boolean undo;
    private Boolean clear;

    public StrokeDTO() {}

    public StrokeDTO(String tool, String color, double width, List<Double> xPoints, List<Double> yPoints) {
        this.tool = tool;
        this.color = color;
        this.width = width;
        this.xPoints = xPoints;
        this.yPoints = yPoints;
    }

    public String getTool() {
        if (tool != null) return tool;
        if (Boolean.TRUE.equals(clear)) return TOOL_CLEAR;
        if (Boolean.TRUE.equals(undo)) return "UNDO";
        return TOOL_BRUSH;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String getColor() {
        return color != null ? color : "#000000";
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getWidth() {
        if (width > 0) return width;
        return 3.0;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public List<Double> getxPoints() { return xPoints; }
    public void setxPoints(List<Double> xPoints) { this.xPoints = xPoints; }

    public List<Double> getyPoints() { return yPoints; }
    public void setyPoints(List<Double> yPoints) { this.yPoints = yPoints; }

    @JsonIgnore
    public boolean isClear() {
        return TOOL_CLEAR.equals(tool) || Boolean.TRUE.equals(clear);
    }

    @JsonIgnore
    public boolean isUndo() {
        return "UNDO".equals(tool) || Boolean.TRUE.equals(undo);
    }

    public Boolean getUndo() { return undo; }
    public void setUndo(Boolean undo) { this.undo = undo; }

    public Boolean getClear() { return clear; }
    public void setClear(Boolean clear) { this.clear = clear; }

}
