package com.github.mdeluise.ytsms.scraper.ytapi;

import com.github.mdeluise.ytsms.video.Video;

import java.io.IOException;
import java.util.Collection;

public interface VideoApiExtractor {
    Collection<Video> getVideo(String... channelIds) throws IOException;
}
