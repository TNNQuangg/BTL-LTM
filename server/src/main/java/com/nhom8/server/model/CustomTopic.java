package com.nhom8.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "custom_topics", uniqueConstraints = @UniqueConstraint(columnNames = "word"))
public class CustomTopic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String word;

    public CustomTopic() {}
    public CustomTopic(String word) { this.word = word; }
    public Long getId() { return id; }
    public String getWord() { return word; }
}
