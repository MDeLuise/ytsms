package com.github.mdeluise.ytsms.scraper.rss;

import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.channel.ChannelRepository;
import com.github.mdeluise.ytsms.exception.ResourceNotFoundException;
import com.github.mdeluise.ytsms.video.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Date;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;

@Component
@Primary
@Profile("!integration")
public class VideoFeedExtractorImpl implements VideoFeedExtractor {
    private final String rssUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=";
    private final ChannelRepository channelRepository;


    @Autowired
    public VideoFeedExtractorImpl(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }


    @Override
    public Collection<Video> getVideo(String... channelIds) throws IOException, SAXException, ParserConfigurationException {
        Collection<Video> toReturn = new HashSet<>();
        for (String channelId : channelIds) {
            toReturn.addAll(getVideo(channelId));
        }
        return toReturn;
    }


    private Collection<Video> getVideo(String channelId) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        String pageContent = getPageContent(rssUrl + channelId);
        Document doc = builder.parse(new InputSource(new StringReader(pageContent)));
        doc.getDocumentElement().normalize();

        Collection<Video> toReturn = new HashSet<>();
        NodeList entries = doc.getElementsByTagName("entry");
        setChannelNameIfNotAlreadyDone(channelId, doc);
        for (int i = 0; i < entries.getLength(); i++) {
            toReturn.add(xmlEntryToVideo(channelId, entries.item(i)));
        }
        return toReturn;
    }


    private String getPageContent(String rssUrl) throws IOException {
        URL url = new URL(rssUrl);
        Scanner sc = new Scanner(url.openStream());
        StringBuilder sb = new StringBuilder();
        while (sc.hasNext()) {
            sb.append(sc.next());
            sb.append(' ');
        }
        return sb.toString();
    }


    private void setChannelNameIfNotAlreadyDone(String channelId, Document doc) {
        Channel channel =
            channelRepository.findById(channelId).orElseThrow(() -> new ResourceNotFoundException("id", channelId));
        if (channel.getName() != null) {
            return;
        }
        NodeList titleNode = doc.getElementsByTagName("title");
        String title = titleNode.item(0).getFirstChild().getNodeValue();
        channel.setName(title.strip());
        channelRepository.save(channel);
    }


    private Video xmlEntryToVideo(String channelId, Node item) {
        Video video = new Video();
        Channel channel =
            channelRepository.findById(channelId).orElseThrow(() -> new ResourceNotFoundException("id", channelId));
        video.setChannel(channel);
        NodeList childNodes = item.getChildNodes();
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
        NodeList mediaItems = item.getChildNodes();
        for (int i = 0; i < mediaItems.getLength(); i++) {
            if (mediaItems.item(i).getNodeName().equals("media:thumbnail")) {
                String thumbnailLink = mediaItems.item(i).getAttributes().getNamedItem("url").getNodeValue();
                video.setThumbnailLink(thumbnailLink);
            } else if (mediaItems.item(i).getNodeName().equals("media:community")) {
                setViewCounter(video, mediaItems.item(i));
            }
        }
    }


    private void setViewCounter(Video video, Node item) {
        NodeList mediaItems = item.getChildNodes();
        for (int i = 0; i < mediaItems.getLength(); i++) {
            if (mediaItems.item(i).getNodeName().equals("media:statistics")) {
                String views = mediaItems.item(i).getAttributes().getNamedItem("views").getNodeValue();
                video.setView(Long.parseLong(views));
            }
        }
    }
}
