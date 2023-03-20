package com.github.mdeluise.ytsms.scraper;

public class FetchVideoException extends RuntimeException {
    public FetchVideoException(String videoId) {
        super(String.format("Error while fetching video for channel: %s", videoId));
    }


    public FetchVideoException(String videoId, Throwable cause) {
        super(String.format("Error while fetching video for channel: %s", videoId), cause);
    }


    public FetchVideoException(Throwable cause) {
        super("Error while fetching video for channel", cause);
    }
}
