package com.github.mdeluise.ytsms.scraper.rss;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

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

@Component
public class VideoRssScraper implements VideoScraper {
    private final VideoFeedExtractor videoFeedExtractor;
    private final VideoService videoService;
    private final ChannelService channelService;
    private Date lastFetchDate;
    private boolean scraping;
    private boolean isLastFailed;
    private final Logger logger = LoggerFactory.getLogger(VideoRssScraper.class);


    @Autowired
    public VideoRssScraper(VideoFeedExtractor videoFeedExtractor, VideoService videoService,
                           ChannelService channelService) {
        this.videoFeedExtractor = videoFeedExtractor;
        this.videoService = videoService;
        this.channelService = channelService;
    }


    @Override
    public void saveNewVideo() throws FetchVideoException {
        final Set<Channel> channels = new HashSet<>(channelService.getAll());
        saveNewVideo(channels);
    }


    @Override
    public void saveNewVideo(Set<Channel> channels) throws FetchVideoException {
        scraping = true;
        for (Channel channel : channels) {
            final Collection<Video> channelVideo;
            try {
                channelVideo = videoFeedExtractor.getVideo(channel.getId());
            } catch (IOException | SAXException | ParserConfigurationException e) {
                lastFetchDate = new Date();
                isLastFailed = true;
                scraping = false;
                logger.error("Error while fetching video for channel with name {} and id {}", channel.getName(),
                             channel.getId()
                );
                throw new FetchVideoException(channel.getId(), e);
            }
            for (Video video : channelVideo) {
                if (!videoService.exists(video.getId())) {
                    logger.info("Saving new video with title {} and id {}...", video.getTitle(), video.getId());
                    videoService.save(video);
                }
            }
            lastFetchDate = new Date();
            scraping = false;
            isLastFailed = false;
        }
    }


    @Override
    public ScraperStatus getStatus() {
        return new ScraperStatus(lastFetchDate, scraping, isLastFailed);
    }
}
