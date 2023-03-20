package com.github.mdeluise.ytsms.scraper.ytapi;

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

import java.io.IOException;
import java.util.Collection;

@Component
public class VideoApiScraper implements VideoScraper {
    private final VideoApiExtractor videoApiExtractor;
    private final VideoService videoService;
    private final ChannelService channelService;
    private final Logger log = LoggerFactory.getLogger(VideoApiScraper.class);


    @Autowired
    public VideoApiScraper(VideoApiExtractor videoApiExtractor, VideoService videoService,
                           ChannelService channelService) {
        this.videoApiExtractor = videoApiExtractor;
        this.videoService = videoService;
        this.channelService = channelService;
    }


    @Override
    public void saveNewVideo() {
        Collection<String> channelIds = channelService.getAll().stream().map(Channel::getId).toList();

            Collection<Video> channelVideo;
            try {
                channelVideo = videoApiExtractor.getVideo(channelIds.toArray(String[]::new));
            } catch (IOException e) {
                throw new FetchVideoException(e);
            }
            for (Video video : channelVideo) {
                if (!videoService.exists(video.getId())) {
                    log.info("Saved new video: " + video);
                    videoService.save(video);
                }
            }
    }


    @Override
    public void saveNewVideo(Channel channel) {
        Collection<Video> channelVideo;
        try {
            channelVideo = videoApiExtractor.getVideo(channel.getId());
        } catch (IOException e) {
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
