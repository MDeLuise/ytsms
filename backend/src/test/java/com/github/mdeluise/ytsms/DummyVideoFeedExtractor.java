package com.github.mdeluise.ytsms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.github.mdeluise.ytsms.scraper.rss.VideoFeedExtractor;
import com.github.mdeluise.ytsms.video.Video;
import org.springframework.stereotype.Component;

@Component
public class DummyVideoFeedExtractor implements VideoFeedExtractor {
    private final Map<String, Collection<Video>> channelsVideo = new HashMap<>();


    @Override
    public Collection<Video> getVideo(String... channelIds) {
        final Collection<Video> video = new HashSet<>();
        for (String channelId : channelIds) {
            video.addAll(channelsVideo.get(channelId));
        }
        return video;
    }


    public void setupVideoForChannel(String channelId, Collection<Video> video) {
        channelsVideo.put(channelId, video);
    }


    public void addVideoForChannel(String channelId, Video video) {
        final Collection<Video> settedVideo = channelsVideo.get(channelId);
        if (settedVideo != null) {
            settedVideo.add(video);
        } else {
            channelsVideo.put(channelId, new ArrayList<>(List.of(video)));
        }
    }
}
