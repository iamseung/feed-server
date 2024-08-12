package com.example.feed_server.dto;

public class FeedRequest {
    private String imageId;
    private int uploaderId;
    private String contents;

    public String getImageId() {
        return imageId;
    }

    public int getUploaderId() {
        return uploaderId;
    }

    public String getContents() {
        return contents;
    }
}
