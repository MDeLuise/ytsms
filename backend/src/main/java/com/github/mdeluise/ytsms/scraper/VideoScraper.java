package com.github.mdeluise.ytsms.scraper;

import com.github.mdeluise.ytsms.channel.Channel;

public interface VideoScraper {
    void saveNewVideo();

    void saveNewVideo(Channel channel);
}
