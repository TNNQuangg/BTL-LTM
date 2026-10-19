package com.nhom8.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Persistent edits to the source-defined built-in topic list. A null replacement means removed. */
@Entity
@Table(name = "built_in_topic_overrides", uniqueConstraints =
        @UniqueConstraint(columnNames = "original_word"))
public class BuiltInTopicOverride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_word", nullable = false, length = 100)
    private String originalWord;

    @Column(name = "replacement_word", length = 100)
    private String replacementWord;

    public BuiltInTopicOverride() {}

    public BuiltInTopicOverride(String originalWord, String replacementWord) {
        this.originalWord = originalWord;
        this.replacementWord = replacementWord;
    }

    public Long getId() { return id; }
    public String getOriginalWord() { return originalWord; }
    public String getReplacementWord() { return replacementWord; }
    public void setReplacementWord(String replacementWord) { this.replacementWord = replacementWord; }
}
