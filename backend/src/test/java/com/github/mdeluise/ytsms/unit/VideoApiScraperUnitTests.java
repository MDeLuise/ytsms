package com.github.mdeluise.ytsms.unit;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.channel.ChannelService;
import com.github.mdeluise.ytsms.scraper.FetchVideoException;
import com.github.mdeluise.ytsms.scraper.ScraperStatus;
import com.github.mdeluise.ytsms.scraper.ytapi.VideoApiExtractor;
import com.github.mdeluise.ytsms.scraper.ytapi.VideoApiScraper;
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

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for VideoApiScraper")
public class VideoApiScraperUnitTests {
    @Mock
    private VideoApiExtractor videoApiExtractor;
    @Mock
    private VideoService videoService;
    @Mock
    private ChannelService channelService;
    @InjectMocks
    private VideoApiScraper videoApiScraper;


    @BeforeEach
    public void setUp() {
        videoApiScraper = new VideoApiScraper(videoApiExtractor, videoService, channelService);
    }


    @Test
    @DisplayName("Should save new video successfully")
    public void shouldSaveNewVideoSuccessfully() throws IOException {
        final Channel channel = new Channel();
        channel.setId("channel-id-1");
        final Video video = new Video();
        video.setId("video-id-1");
        final Set<Channel> channels = new HashSet<>();
        channels.add(channel);
        final Collection<Video> channelVideo = new HashSet<>();
        channelVideo.add(video);
        Mockito.when(channelService.getAll()).thenReturn(channels);
        Mockito.when(videoApiExtractor.getVideo(channel.getId())).thenReturn(channelVideo);
        Mockito.when(videoService.exists(video.getId())).thenReturn(false);

        videoApiScraper.saveNewVideo();

        Mockito.verify(videoService, Mockito.times(1)).save(video);
    }


    @Test
    @DisplayName("Should get scraper status after successful extraction")
    public void shouldGetScraperStatusAfterSuccessfulExtraction() throws IOException {
        final Channel channel = new Channel();
        channel.setId("channel-id-1");
        final Set<Channel> channels = new HashSet<>();
        channels.add(channel);
        final Collection<Video> channelVideo = new HashSet<>();
        Mockito.when(channelService.getAll()).thenReturn(channels);
        Mockito.when(videoApiExtractor.getVideo(channel.getId())).thenReturn(channelVideo);
        Mockito.when(videoService.exists(Mockito.anyString())).thenReturn(false);

        videoApiScraper.saveNewVideo();

        final ScraperStatus status = videoApiScraper.getStatus();
        Assertions.assertThat(status.lastScrape()).isNotNull();
        Assertions.assertThat(status.scraping()).isFalse();
        Assertions.assertThat(status.isLastFailed()).isFalse();
    }


    @Test
    @DisplayName("Should get scraper status after failed extraction")
    public void shouldGetScraperStatusAfterFailedExtraction() throws IOException {
        final Channel channel = new Channel();
        channel.setId("channel-id-1");
        final Set<Channel> channels = new HashSet<>();
        channels.add(channel);
        Mockito.when(channelService.getAll()).thenReturn(channels);
        Mockito.when(videoApiExtractor.getVideo(channel.getId())).thenThrow(IOException.class);

        Assertions.assertThatThrownBy(() -> videoApiScraper.saveNewVideo()).isInstanceOf(FetchVideoException.class);

        final ScraperStatus status = videoApiScraper.getStatus();
        Assertions.assertThat(status.lastScrape()).isNotNull();
        Assertions.assertThat(status.scraping()).isFalse();
        Assertions.assertThat(status.isLastFailed()).isTrue();
    }
}

