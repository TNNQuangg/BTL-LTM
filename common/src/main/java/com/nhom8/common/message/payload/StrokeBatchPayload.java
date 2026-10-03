package com.nhom8.common.message.payload;

import com.nhom8.common.dto.StrokeDTO;
import java.util.List;

public class StrokeBatchPayload {
    private List<StrokeDTO> strokes;

    public StrokeBatchPayload() {}

    public StrokeBatchPayload(List<StrokeDTO> strokes) {
        this.strokes = strokes;
    }

    public List<StrokeDTO> getStrokes() { return strokes; }
    public void setStrokes(List<StrokeDTO> strokes) { this.strokes = strokes; }
}
