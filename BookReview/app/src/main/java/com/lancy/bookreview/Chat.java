package com.lancy.bookreview;

public class Chat {
    public String message;
    public String from;
    public String to;
    public long timestamp;

    private Chat() {}

    public Chat(String message, String from, String to, long timestamp) {
        this.message = message;
        this.from = from;
        this.to = to;
        this.timestamp = timestamp;
    }
}
