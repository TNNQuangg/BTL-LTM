package com.nhom8.vehinhdoany.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Thực thể lưu trữ thông tin bức tranh trong một trận đấu.
 * Mỗi bức tranh thuộc về một trận đấu cụ thể, do một người chơi vẽ,
 * gắn liền với một chủ đề riêng biệt.
 */
@Entity
@Table(name = "paintings")
public class Painting implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "painting_id")
    private Integer paintingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "painter_id", nullable = false)
    private User painter;

    @Column(nullable = false, length = 100)
    private String topic;

    @Lob
    @Column(name = "stroke_data", columnDefinition = "LONGTEXT")
    private String strokeData;

    public Painting() {}

    public Painting(Match match, User painter, String topic) {
        this.match = match;
        this.painter = painter;
        this.topic = topic;
    }

    public Integer getPaintingId() { return paintingId; }
    public void setPaintingId(Integer paintingId) { this.paintingId = paintingId; }
    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }
    public User getPainter() { return painter; }
    public void setPainter(User painter) { this.painter = painter; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getStrokeData() { return strokeData; }
    public void setStrokeData(String strokeData) { this.strokeData = strokeData; }

    @Override
    public String toString() {
        return "Painting{paintingId=" + paintingId + ", painterId=" + (painter != null ? painter.getUserId() : "null") +
                ", topic='" + topic + "'}";
    }
}
