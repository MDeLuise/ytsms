package com.github.mdeluise.ytsms;

import com.github.mdeluise.ytsms.scraper.VideoFeedScraper;
import com.github.mdeluise.ytsms.video.Video;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DummyVideoFeedScraper implements VideoFeedScraper {
    private final Map<String, Collection<Video>> channelsVideo = new HashMap<>();


    @Override
    public Collection<Video> getVideo(String channelId) {
        Collection<Video> videos = channelsVideo.get(channelId);
        if (videos != null) {
            return videos;
        }
        return Collections.emptyList();
    }


    public void setupVideoForChannel(String channelId, Collection<Video> video) {
        channelsVideo.put(channelId, video);
    }


    public void addVideoForChannel(String channelId, Video video) {
        Collection<Video> settedVideo = channelsVideo.get(channelId);
        if (settedVideo != null) {
            settedVideo.add(video);
        } else {
            channelsVideo.put(channelId, new ArrayList<>(List.of(video)));
        }
    }
}
