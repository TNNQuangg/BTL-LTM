package com.nhom8.common.message.payload;

public class TopicSelectPayload {
    private int topicId;

    public TopicSelectPayload() {}

    public TopicSelectPayload(int topicId) {
        this.topicId = topicId;
    }

    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }
}
