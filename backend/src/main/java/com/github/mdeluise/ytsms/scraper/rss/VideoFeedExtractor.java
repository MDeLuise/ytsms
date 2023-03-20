package com.github.mdeluise.ytsms.scraper.rss;

import java.io.IOException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;

import com.github.mdeluise.ytsms.video.Video;
import org.xml.sax.SAXException;

public interface VideoFeedExtractor {
    Collection<Video> getVideo(String... channelIds) throws IOException, SAXException, ParserConfigurationException;
}
