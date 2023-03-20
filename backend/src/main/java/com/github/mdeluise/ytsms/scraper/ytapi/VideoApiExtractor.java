package com.github.mdeluise.ytsms.scraper.ytapi;

import java.io.IOException;
import java.util.Collection;

import com.github.mdeluise.ytsms.video.Video;

public interface VideoApiExtractor {
    Collection<Video> getVideo(String... channelIds) throws IOException;
}
