package com.nhom8.vehinhdoany.protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đóng gói dữ liệu nét vẽ (stroke data) để truyền qua WebSocket dạng JSON.
 */
public class DrawData {

    private List<Integer> xPoints;
    private List<Integer> yPoints;
    private int colorRGB;
    private int strokeWidth;
    private boolean isEraser;
    private boolean clear;
    private boolean undo;

    public DrawData() {
        this.xPoints = new ArrayList<>();
        this.yPoints = new ArrayList<>();
        this.colorRGB = 0x000000;
        this.strokeWidth = 3;
        this.isEraser = false;
        this.clear = false;
        this.undo = false;
    }

    public DrawData(int colorRGB, int strokeWidth) {
        this.xPoints = new ArrayList<>();
        this.yPoints = new ArrayList<>();
        this.colorRGB = colorRGB;
        this.strokeWidth = strokeWidth;
        this.isEraser = false;
        this.clear = false;
        this.undo = false;
    }

    public void addPoint(int x, int y) { xPoints.add(x); yPoints.add(y); }

    public List<Integer> getXPoints() { return xPoints; }
    public void setXPoints(List<Integer> xPoints) { this.xPoints = xPoints; }
    public List<Integer> getYPoints() { return yPoints; }
    public void setYPoints(List<Integer> yPoints) { this.yPoints = yPoints; }
    public int getColorRGB() { return colorRGB; }
    public void setColorRGB(int colorRGB) { this.colorRGB = colorRGB; }
    public int getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(int strokeWidth) { this.strokeWidth = strokeWidth; }
    public boolean isEraser() { return isEraser; }
    public void setEraser(boolean eraser) { isEraser = eraser; }
    public boolean isClear() { return clear; }
    public void setClear(boolean clear) { this.clear = clear; }
    public boolean isUndo() { return undo; }
    public void setUndo(boolean undo) { this.undo = undo; }
    public int getPointCount() { return xPoints.size(); }
    public String getColorHex() { return String.format("#%06X", colorRGB & 0xFFFFFF); }

    @Override
    public String toString() {
        return "DrawData{points=" + getPointCount() + ", color=" + getColorHex() +
                ", width=" + strokeWidth + ", eraser=" + isEraser + '}';
    }
}
