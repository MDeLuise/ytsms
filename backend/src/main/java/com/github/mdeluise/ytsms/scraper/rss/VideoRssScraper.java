package com.github.mdeluise.ytsms.scraper.rss;

import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.channel.ChannelService;
import com.github.mdeluise.ytsms.scraper.FetchVideoException;
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

@Component
public class VideoRssScraper implements VideoScraper {
    private final VideoFeedExtractor videoFeedExtractor;
    private final VideoService videoService;
    private final ChannelService channelService;
    private final Logger log = LoggerFactory.getLogger(VideoRssScraper.class);


    @Autowired
    public VideoRssScraper(VideoFeedExtractor videoFeedExtractor, VideoService videoService, ChannelService channelService) {
        this.videoFeedExtractor = videoFeedExtractor;
        this.videoService = videoService;
        this.channelService = channelService;
    }


    @Override
    public void saveNewVideo() {
        Collection<Channel> channels = channelService.getAll();
        for (Channel channel : channels) {
            Collection<Video> channelVideo;
            try {
                channelVideo = videoFeedExtractor.getVideo(channel.getId());
            } catch (IOException | SAXException | ParserConfigurationException e) {
                throw new FetchVideoException(channel.getId(), e);
            }
            for (Video video : channelVideo) {
                if (!videoService.exists(video.getId())) {
                    log.info("Saved new video: " + video);
                    videoService.save(video);
                }
            }
        }
    }


    @Override
    public void saveNewVideo(Channel channel) {
        Collection<Video> channelVideo;
        try {
            channelVideo = videoFeedExtractor.getVideo(channel.getId());
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new FetchVideoException(channel.getId(), e);
        }
        for (Video video : channelVideo) {
            if (!videoService.exists(video.getId())) {
                log.info("Saved new video: " + video);
                videoService.save(video);
            }
        }
    }
}
