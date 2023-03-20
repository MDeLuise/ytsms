package com.github.mdeluise.ytsms.scraper.ytapi;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.channel.ChannelService;
import com.github.mdeluise.ytsms.scraper.FetchVideoException;
import com.github.mdeluise.ytsms.scraper.ScraperStatus;
import com.github.mdeluise.ytsms.scraper.VideoScraper;
import com.github.mdeluise.ytsms.video.Video;
import com.github.mdeluise.ytsms.video.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VideoApiScraper implements VideoScraper {
    private final VideoApiExtractor videoApiExtractor;
    private final VideoService videoService;
    private final ChannelService channelService;
    private Date lastFetchDate;
    private boolean scraping;
    private boolean isLastFailed;
    private final Logger logger = LoggerFactory.getLogger(VideoApiScraper.class);


    @Autowired
    public VideoApiScraper(VideoApiExtractor videoApiExtractor, VideoService videoService,
                           ChannelService channelService) {
        this.videoApiExtractor = videoApiExtractor;
        this.videoService = videoService;
        this.channelService = channelService;
    }


    @Override
    public void saveNewVideo() {
        scraping = true;
        final Set<Channel> channels = new HashSet<>(channelService.getAll());
        saveNewVideo(channels);
    }


    @Override
    public void saveNewVideo(Set<Channel> channels) {
        logger.debug("Fetching new video from {} channels: {}", channels.size(), channels);
        final Collection<Video> channelVideo;
        try {
            channelVideo = videoApiExtractor.getVideo(channels.stream().map(Channel::getId).toArray(String[]::new));
        } catch (IOException e) {
            logger.error("Error while fetching videos from channels {}", channels, e);
            lastFetchDate = new Date();
            scraping = false;
            isLastFailed = true;
            throw new FetchVideoException(e);
        }
        for (Video video : channelVideo) {
            if (!videoService.exists(video.getId())) {
                logger.info("Saving new video with name {} and id {}...", video.getTitle(), video.getId());
                videoService.save(video);
            }
        }
        lastFetchDate = new Date();
        scraping = false;
        isLastFailed = false;
    }


    @Override
    public ScraperStatus getStatus() {
        return new ScraperStatus(lastFetchDate, scraping, isLastFailed);
    }
}
