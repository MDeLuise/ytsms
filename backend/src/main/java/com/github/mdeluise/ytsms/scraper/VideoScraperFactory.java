package com.github.mdeluise.ytsms.scraper;

import com.github.mdeluise.ytsms.scraper.rss.VideoRssScraper;
import com.github.mdeluise.ytsms.scraper.ytapi.VideoApiScraper;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VideoScraperFactory {
    private final String youtubeApi;
    private final VideoRssScraper videoRssScraper;
    private final VideoApiScraper videoApiScraper;


    @Autowired
    public VideoScraperFactory(@Value("${youtube.key}") String youtubeApi, VideoRssScraper videoRssScraper,
                               VideoApiScraper videoApiScraper) {
        this.youtubeApi = youtubeApi;
        this.videoRssScraper = videoRssScraper;
        this.videoApiScraper = videoApiScraper;
    }


    public VideoScraper getVideoScraper() {
        return Strings.isNullOrEmpty(youtubeApi) ? videoRssScraper : videoApiScraper;
    }
}
