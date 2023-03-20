package com.github.mdeluise.ytsms.scraper;

public class FetchVideoException extends RuntimeException {
    public FetchVideoException(String channelId, Throwable cause) {
        super(String.format("Error while fetching video for channel: %s", channelId), cause);
    }


    public FetchVideoException(Throwable cause) {
        super("Error while fetching video for channel", cause);
    }
}
