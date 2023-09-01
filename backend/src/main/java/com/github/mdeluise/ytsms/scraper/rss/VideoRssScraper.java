package com.github.mdeluise.ytsms.scraper.rss;

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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

@Component
public class VideoRssScraper implements VideoScraper {
    private final VideoFeedExtractor videoFeedExtractor;
    private final VideoService videoService;
    private final ChannelService channelService;
    private Date lastFetchDate;
    private boolean scraping;
    private boolean isLastFailed;
    private final Logger log = LoggerFactory.getLogger(VideoRssScraper.class);


    @Autowired
    public VideoRssScraper(VideoFeedExtractor videoFeedExtractor, VideoService videoService,
                           ChannelService channelService) {
        this.videoFeedExtractor = videoFeedExtractor;
        this.videoService = videoService;
        this.channelService = channelService;
    }


    @Override
    public void saveNewVideo() {
        scraping = true;
        Collection<Channel> channels = channelService.getAll();
        for (Channel channel : channels) {
            Collection<Video> channelVideo;
            try {
                channelVideo = videoFeedExtractor.getVideo(channel.getId());
            } catch (IOException | SAXException | ParserConfigurationException e) {
                isLastFailed = true;
            scraping = false;
                throw new FetchVideoException(channel.getId(), e);
            }
            for (Video video : channelVideo) {
                if (!videoService.exists(video.getId())) {
                    log.info("Saved new video: " + video);
                    videoService.save(video);
                }
            }
            lastFetchDate = new Date();
        scraping = false;
        isLastFailed = false;
        }
    }


    @Override
    public void saveNewVideo(Channel channel) {
        scraping = true;
        Collection<Video> channelVideo;
        try {
            channelVideo = videoFeedExtractor.getVideo(channel.getId());
        } catch (IOException | SAXException | ParserConfigurationException e) {
            isLastFailed = true;
            scraping = false;
            throw new FetchVideoException(channel.getId(), e);
        }
        for (Video video : channelVideo) {
            if (!videoService.exists(video.getId())) {
                log.info("Saved new video: " + video);
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
