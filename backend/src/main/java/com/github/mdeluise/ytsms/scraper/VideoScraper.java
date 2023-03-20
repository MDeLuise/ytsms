package com.github.mdeluise.ytsms.scraper;

import java.util.Set;

import com.github.mdeluise.ytsms.channel.Channel;

public interface VideoScraper {
    void saveNewVideo() throws FetchVideoException;

    void saveNewVideo(Set<Channel> channels) throws FetchVideoException;

    ScraperStatus getStatus();
}
