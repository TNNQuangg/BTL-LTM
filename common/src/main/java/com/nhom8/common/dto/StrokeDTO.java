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

    // React Client compatibility fields
    private Boolean undo;
    private Boolean clear;
    private Object colorRGB;
    private Double strokeWidth;
    private Boolean isEraser;

    public StrokeDTO() {}

    public StrokeDTO(String tool, String color, double width, List<Double> xPoints, List<Double> yPoints) {
        this.tool = tool;
        this.color = color;
        this.width = width;
        this.xPoints = xPoints;
        this.yPoints = yPoints;
        this.strokeWidth = width;
        this.isEraser = TOOL_ERASER.equals(tool);
        if (color != null) {
            try {
                String hex = color.startsWith("#") ? color.substring(1) : color;
                this.colorRGB = Integer.parseInt(hex, 16);
            } catch (Exception ignored) {
                this.colorRGB = 0x000000;
            }
        } else {
            this.colorRGB = 0x000000;
        }
    }

    public String getTool() {
        if (tool != null) return tool;
        if (Boolean.TRUE.equals(clear)) return TOOL_CLEAR;
        if (Boolean.TRUE.equals(undo)) return "UNDO";
        if (Boolean.TRUE.equals(isEraser)) return TOOL_ERASER;
        return TOOL_BRUSH;
    }

    public void setTool(String tool) {
        this.tool = tool;
        if (this.isEraser == null) {
            this.isEraser = TOOL_ERASER.equals(tool);
        }
    }

    public String getColor() {
        if (color != null) return color;
        if (colorRGB != null) {
            if (colorRGB instanceof Number) {
                return String.format("#%06X", (0xFFFFFF & ((Number) colorRGB).intValue()));
            } else {
                return colorRGB.toString();
            }
        }
        return "#000000";
    }

    public void setColor(String color) {
        this.color = color;
        if (color != null && this.colorRGB == null) {
            try {
                String hex = color.startsWith("#") ? color.substring(1) : color;
                this.colorRGB = Integer.parseInt(hex, 16);
            } catch (Exception ignored) {}
        }
    }

    public double getWidth() {
        if (width > 0) return width;
        if (strokeWidth != null && strokeWidth > 0) return strokeWidth;
        return 3.0;
    }

    public void setWidth(double width) {
        this.width = width;
        if (this.strokeWidth == null) {
            this.strokeWidth = width;
        }
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

    public Object getColorRGB() {
        if (colorRGB != null) return colorRGB;
        if (color != null) {
            try {
                String hex = color.startsWith("#") ? color.substring(1) : color;
                return Integer.parseInt(hex, 16);
            } catch (Exception ignored) {}
        }
        return 0x000000;
    }

    public void setColorRGB(Object colorRGB) {
        this.colorRGB = colorRGB;
        if (colorRGB != null && this.color == null) {
            if (colorRGB instanceof Number) {
                this.color = String.format("#%06X", (0xFFFFFF & ((Number) colorRGB).intValue()));
            } else {
                this.color = colorRGB.toString();
            }
        }
    }

    public Double getStrokeWidth() {
        if (strokeWidth != null && strokeWidth > 0) return strokeWidth;
        return width > 0 ? width : 3.0;
    }

    public void setStrokeWidth(Double strokeWidth) {
        this.strokeWidth = strokeWidth;
        if (strokeWidth != null && this.width <= 0) {
            this.width = strokeWidth;
        }
    }

    public Boolean getIsEraser() {
        if (isEraser != null) return isEraser;
        return TOOL_ERASER.equals(tool);
    }

    public void setIsEraser(Boolean isEraser) {
        this.isEraser = isEraser;
        if (Boolean.TRUE.equals(isEraser) && this.tool == null) {
            this.tool = TOOL_ERASER;
        }
    }
}
