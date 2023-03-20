package com.github.mdeluise.ytsms.scraper;

import com.github.mdeluise.ytsms.video.Video;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Collection;

public interface VideoFeedScraper {
    Collection<Video> getVideo(String channelId) throws IOException, SAXException, ParserConfigurationException;
}
