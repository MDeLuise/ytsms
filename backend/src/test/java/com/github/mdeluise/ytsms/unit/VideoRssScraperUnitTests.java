package com.github.mdeluise.ytsms.unit;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.channel.ChannelService;
import com.github.mdeluise.ytsms.scraper.FetchVideoException;
import com.github.mdeluise.ytsms.scraper.ScraperStatus;
import com.github.mdeluise.ytsms.scraper.rss.VideoFeedExtractor;
import com.github.mdeluise.ytsms.scraper.rss.VideoRssScraper;
import com.github.mdeluise.ytsms.video.Video;
import com.github.mdeluise.ytsms.video.VideoService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.xml.sax.SAXException;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for VideoRssScraper")
public class VideoRssScraperUnitTests {
    @Mock
    private VideoFeedExtractor videoFeedExtractor;
    @Mock
    private VideoService videoService;
    @Mock
    private ChannelService channelService;
    @InjectMocks
    private VideoRssScraper videoRssScraper;


    @BeforeEach
    public void setUp() {
        videoRssScraper = new VideoRssScraper(videoFeedExtractor, videoService, channelService);
    }


    @Test
    @DisplayName("Should save new video successfully")
    public void shouldSaveNewVideoSuccessfully() throws IOException, SAXException, ParserConfigurationException {
        final Channel channel = new Channel();
        channel.setId("channel-id-1");
        final Video video = new Video();
        video.setId("video-id-1");
        final Set<Channel> channels = new HashSet<>();
        channels.add(channel);
        final Collection<Video> channelVideos = new HashSet<>();
        channelVideos.add(video);
        Mockito.when(channelService.getAll()).thenReturn(channels);
        Mockito.when(videoFeedExtractor.getVideo(channel.getId())).thenReturn(channelVideos);
        Mockito.when(videoService.exists(video.getId())).thenReturn(false);

        videoRssScraper.saveNewVideo();

        Mockito.verify(videoService, Mockito.times(1)).save(video);
    }


    @Test
    @DisplayName("Should return error on failed video extraction")
    public void shouldThrowExceptionOnFailedVideoExtraction()
        throws IOException, SAXException, ParserConfigurationException {
        final Channel channel = new Channel();
        channel.setId("channel-id-1");
        final Set<Channel> channels = new HashSet<>();
        channels.add(channel);
        Mockito.when(channelService.getAll()).thenReturn(channels);
        Mockito.when(videoFeedExtractor.getVideo(channel.getId())).thenThrow(IOException.class);

        Assertions.assertThatThrownBy(() -> videoRssScraper.saveNewVideo()).isInstanceOf(FetchVideoException.class);
    }


    @Test
    @DisplayName("Should get scraper status after successful extraction")
    public void shouldGetScraperStatusAfterSuccessfulExtraction()
        throws IOException, SAXException, ParserConfigurationException {
        final Channel channel = new Channel();
        channel.setId("channel-id-1");
        final Set<Channel> channels = new HashSet<>();
        channels.add(channel);
        final Collection<Video> channelVideos = new HashSet<>();
        Mockito.when(channelService.getAll()).thenReturn(channels);
        Mockito.when(videoFeedExtractor.getVideo(channel.getId())).thenReturn(channelVideos);
        Mockito.when(videoService.exists(Mockito.anyString())).thenReturn(false);

        videoRssScraper.saveNewVideo();

        final ScraperStatus status = videoRssScraper.getStatus();
        Assertions.assertThat(status.lastScrape()).isNotNull();
        Assertions.assertThat(status.scraping()).isFalse();
        Assertions.assertThat(status.isLastFailed()).isFalse();
    }


    @Test
    @DisplayName("Should get scraper status after failed extraction")
    public void shouldGetScraperStatusAfterFailedExtraction()
        throws IOException, SAXException, ParserConfigurationException {
        final Channel channel = new Channel();
        channel.setId("channel-id-1");
        final Set<Channel> channels = new HashSet<>();
        channels.add(channel);
        Mockito.when(channelService.getAll()).thenReturn(channels);
        Mockito.when(videoFeedExtractor.getVideo(channel.getId())).thenThrow(IOException.class);

        Assertions.assertThatThrownBy(() -> videoRssScraper.saveNewVideo()).isInstanceOf(FetchVideoException.class);

        final ScraperStatus status = videoRssScraper.getStatus();
        Assertions.assertThat(status.lastScrape()).isNotNull();
        Assertions.assertThat(status.scraping()).isFalse();
        Assertions.assertThat(status.isLastFailed()).isTrue();
    }
}
