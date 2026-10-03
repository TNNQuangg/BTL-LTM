package com.nhom8.common.dto;

public class TopicDTO {
    private int id;
    private String content;
    private int wordCount;
    private int charCount;

    public TopicDTO() {}

    public TopicDTO(int id, String content, int wordCount, int charCount) {
        this.id = id;
        this.content = content;
        this.wordCount = wordCount;
        this.charCount = charCount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public int getCharCount() { return charCount; }
    public void setCharCount(int charCount) { this.charCount = charCount; }
}
