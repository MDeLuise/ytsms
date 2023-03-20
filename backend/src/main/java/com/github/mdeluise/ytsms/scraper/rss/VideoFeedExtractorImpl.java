package com.github.mdeluise.ytsms.scraper.rss;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.sql.Date;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.channel.ChannelRepository;
import com.github.mdeluise.ytsms.exception.ResourceNotFoundException;
import com.github.mdeluise.ytsms.scraper.ytapi.VideoApiExtractorImpl;
import com.github.mdeluise.ytsms.video.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Component
@Primary
@Profile("!integration")
public class VideoFeedExtractorImpl implements VideoFeedExtractor {
    private final String rssUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=";
    private final ChannelRepository channelRepository;
    private final Logger logger = LoggerFactory.getLogger(VideoApiExtractorImpl.class);


    @Autowired
    public VideoFeedExtractorImpl(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }


    @Override
    public Collection<Video> getVideo(String... channelIds) throws IOException, SAXException, ParserConfigurationException {
        final Collection<Video> toReturn = new HashSet<>();
        for (String channelId : channelIds) {
            toReturn.addAll(getVideo(channelId));
        }
        return toReturn;
    }


    private Collection<Video> getVideo(String channelId) throws IOException, SAXException, ParserConfigurationException {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final String pageContent = getPageContent(rssUrl + channelId);
        final Document doc = builder.parse(new InputSource(new StringReader(pageContent)));
        doc.getDocumentElement().normalize();

        final Collection<Video> toReturn = new HashSet<>();
        final NodeList entries = doc.getElementsByTagName("entry");
        setChannelNameIfNotAlreadyDone(channelId, doc);
        for (int i = 0; i < entries.getLength(); i++) {
            toReturn.add(xmlEntryToVideo(channelId, entries.item(i)));
        }
        return toReturn;
    }


    private String getPageContent(String rssUrl) throws IOException {
        final URL url = URI.create(rssUrl).toURL();
        final Scanner sc = new Scanner(url.openStream());
        final StringBuilder sb = new StringBuilder();
        while (sc.hasNext()) {
            sb.append(sc.next());
            sb.append(' ');
        }
        return sb.toString();
    }


    private void setChannelNameIfNotAlreadyDone(String channelId, Document doc) {
        final Channel channel =
            channelRepository.findById(channelId).orElseThrow(() -> new ResourceNotFoundException("id", channelId));
        if (channel.getName() != null) {
            return;
        }
        logger.debug("Channel with id {} does not have a name yet, fetching it...", channelId);
        final NodeList titleNode = doc.getElementsByTagName("title");
        final String title = titleNode.item(0).getFirstChild().getNodeValue();
        channel.setName(title.strip());
        channelRepository.save(channel);
    }


    private Video xmlEntryToVideo(String channelId, Node item) {
        final Video video = new Video();
        final Channel channel =
            channelRepository.findById(channelId).orElseThrow(() -> new ResourceNotFoundException("id", channelId));
        video.setChannel(channel);
        final NodeList childNodes = item.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            xmlAttributeToVideoParameter(video, childNodes.item(i + 1));
        }
        return video;
    }


    private void xmlAttributeToVideoParameter(Video video, Node item) {
        if (item == null || Node.TEXT_NODE == item.getNodeType() || !item.hasChildNodes()) {
            return;
        }
        switch (item.getNodeName()) {
            case "yt:videoId" -> video.setId(item.getFirstChild().getNodeValue());
            case "title" -> video.setTitle(item.getFirstChild().getNodeValue());
            case "published" -> video.setPublishedAt(Date.from(Instant.parse(item.getFirstChild().getNodeValue())));
            case "media:group" -> setThumbnailLinkAndViewCount(video, item);
            default -> {
            }
        }
    }


    private void setThumbnailLinkAndViewCount(Video video, Node item) {
        final NodeList mediaItems = item.getChildNodes();
        for (int i = 0; i < mediaItems.getLength(); i++) {
            if (mediaItems.item(i).getNodeName().equals("media:thumbnail")) {
                final String thumbnailLink = mediaItems.item(i).getAttributes().getNamedItem("url").getNodeValue();
                video.setThumbnailLink(thumbnailLink);
            } else if (mediaItems.item(i).getNodeName().equals("media:community")) {
                setViewCounter(video, mediaItems.item(i));
            }
        }
    }


    private void setViewCounter(Video video, Node item) {
        final NodeList mediaItems = item.getChildNodes();
        for (int i = 0; i < mediaItems.getLength(); i++) {
            if (mediaItems.item(i).getNodeName().equals("media:statistics")) {
                final String views = mediaItems.item(i).getAttributes().getNamedItem("views").getNodeValue();
                video.setView(Long.parseLong(views));
            }
        }
    }
}
