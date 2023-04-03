package com.github.mdeluise.ytsms.scraper.rss;

import com.github.mdeluise.ytsms.video.Video;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Collection;

public interface VideoFeedExtractor {
    Collection<Video> getVideo(String... channelIds) throws IOException, SAXException, ParserConfigurationException;
}
