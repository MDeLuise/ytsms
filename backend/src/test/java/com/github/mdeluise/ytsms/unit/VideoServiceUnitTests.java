package com.github.mdeluise.ytsms.unit;

import java.util.List;
import java.util.Set;

import com.github.mdeluise.ytsms.authentication.User;
import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.channel.ChannelService;
import com.github.mdeluise.ytsms.common.AuthenticatedUserService;
import com.github.mdeluise.ytsms.exception.ResourceNotFoundException;
import com.github.mdeluise.ytsms.exception.UnauthorizedException;
import com.github.mdeluise.ytsms.subscription.Subscription;
import com.github.mdeluise.ytsms.subscription.SubscriptionRepository;
import com.github.mdeluise.ytsms.video.Video;
import com.github.mdeluise.ytsms.video.VideoRepository;
import com.github.mdeluise.ytsms.video.VideoService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for VideoService")
public class VideoServiceUnitTests {
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private ChannelService channelService;
    @InjectMocks
    private VideoService videoService;


    @Test
    @DisplayName("Should get all videos for subscribed channels")
    public void shouldGetAllVideosForSubscribedChannels() {
        final User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        final Channel channel1 = new Channel();
        channel1.setId("channel-id-1");
        final Channel channel2 = new Channel();
        channel2.setId("channel-id-2");
        final Subscription subscription1 = new Subscription();
        subscription1.setId(1L);
        subscription1.setChannel(channel1);
        final Subscription subscription2 = new Subscription();
        subscription2.setId(2L);
        subscription2.setChannel(channel2);
        authenticatedUser.setSubscriptions(Set.of(subscription1, subscription2));
        final Pageable pageable = Pageable.unpaged();
        final Video video1 = new Video();
        video1.setId("video-id-1");
        final Video video2 = new Video();
        video2.setId("video-id-2");
        final Page<Video> expectedVideos = new PageImpl<>(List.of(video1, video2));
        Mockito.when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Mockito.when(videoRepository.findByChannelIdIn(Set.of("channel-id-1", "channel-id-2"), pageable))
               .thenReturn(expectedVideos);

        final Page<Video> result = videoService.getAll(pageable);

        Assertions.assertThat(result).isEqualTo(expectedVideos);
        Mockito.verify(authenticatedUserService, Mockito.times(1)).getAuthenticatedUser();
        Mockito.verify(videoRepository, Mockito.times(1))
               .findByChannelIdIn(Set.of("channel-id-1", "channel-id-2"), pageable);
    }



    @Test
    @DisplayName("Should save a video")
    public void shouldSaveVideo() {
        final Video videoToSave = new Video();
        videoToSave.setId("video-id-1");
        Mockito.when(videoRepository.save(videoToSave)).thenReturn(videoToSave);

        final Video result = videoService.save(videoToSave);

        Assertions.assertThat(result).isEqualTo(videoToSave);
        Mockito.verify(videoRepository, Mockito.times(1)).save(videoToSave);
    }


    @Test
    @DisplayName("Should remove a video")
    public void shouldRemoveVideo() {
        final Video videoToRemove = new Video();
        videoToRemove.setId("video-id-1");

        videoService.remove(videoToRemove);

        Mockito.verify(videoRepository, Mockito.times(1)).delete(videoToRemove);
    }


    @Test
    @DisplayName("Should check if a video exists")
    public void shouldCheckIfVideoExists() {
        final String videoId = "video-id";
        Mockito.when(videoRepository.existsById(videoId)).thenReturn(true);

        boolean result = videoService.exists(videoId);

        Assertions.assertThat(result).isTrue();
        Mockito.verify(videoRepository, Mockito.times(1)).existsById(videoId);
    }


    @Test
    @DisplayName("Should remove all videos")
    public void shouldRemoveAllVideos() {
        final Video video1 = new Video();
        video1.setId("video-id-1");
        final Video video2 = new Video();
        video2.setId("video-id-2");
        Mockito.when(videoRepository.findAll()).thenReturn(List.of(video1, video2));

        videoService.removeAll();

        Mockito.verify(videoRepository, Mockito.times(1)).findAll();
        Mockito.verify(videoRepository, Mockito.times(1)).delete(video1);
        Mockito.verify(videoRepository, Mockito.times(1)).delete(video2);
    }


    @Test
    @DisplayName("Should get all videos by channel IDs")
    public void shouldGetAllVideosByChannelIds() {
        final User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        final Channel channel1 = new Channel();
        channel1.setId("channel-id-1");
        final Channel channel2 = new Channel();
        channel2.setId("channel-id-2");
        final List<String> channelIds = List.of("channel-id-1", "channel-id-2");
        Mockito.when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Mockito.when(subscriptionRepository.existsByUserAndChannelId(authenticatedUser, "channel-id-1")).thenReturn(true);
        Mockito.when(subscriptionRepository.existsByUserAndChannelId(authenticatedUser, "channel-id-2")).thenReturn(true);
        Mockito.when(channelService.exists("channel-id-1")).thenReturn(true);
        Mockito.when(channelService.exists("channel-id-2")).thenReturn(true);
        final Pageable pageable = Mockito.mock(Pageable.class);
        final Page<Video> expectedVideos = Mockito.mock(Page.class);
        Mockito.when(videoRepository.findByChannelIdIn(channelIds, pageable)).thenReturn(expectedVideos);

        final Page<Video> result = videoService.getAllByChannelIds(pageable, channelIds);

        Assertions.assertThat(result).isEqualTo(expectedVideos);
        Mockito.verify(authenticatedUserService, Mockito.times(1)).getAuthenticatedUser();
        Mockito.verify(subscriptionRepository, Mockito.times(1))
               .existsByUserAndChannelId(authenticatedUser, "channel-id-1");
        Mockito.verify(subscriptionRepository, Mockito.times(1))
               .existsByUserAndChannelId(authenticatedUser, "channel-id-2");
        Mockito.verify(channelService, Mockito.times(1)).exists("channel-id-1");
        Mockito.verify(channelService, Mockito.times(1)).exists("channel-id-2");
        Mockito.verify(videoRepository, Mockito.times(1)).findByChannelIdIn(channelIds, pageable);
    }


    @Test
    @DisplayName("Should return error when user is not subscribed to a channel")
    public void shouldThrowUnauthorizedExceptionWhenUserNotSubscribedToChannel() {
        final User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        final List<String> channelIds = List.of("channel-id-1", "channel-id-2");
        Mockito.when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Mockito.when(subscriptionRepository.existsByUserAndChannelId(authenticatedUser, "channel-id-1")).thenReturn(false);

        Assertions.assertThatThrownBy(() -> videoService.getAllByChannelIds(Mockito.mock(Pageable.class), channelIds))
                  .isInstanceOf(UnauthorizedException.class);

        Mockito.verify(authenticatedUserService, Mockito.times(1)).getAuthenticatedUser();
        Mockito.verify(subscriptionRepository, Mockito.times(1))
               .existsByUserAndChannelId(authenticatedUser, "channel-id-1");
        Mockito.verify(channelService, Mockito.never()).exists(Mockito.anyString());
        Mockito.verify(videoRepository, Mockito.never()).findByChannelIdIn(Mockito.anyList(), Mockito.any());
    }


    @Test
    @DisplayName("Should return error when channel does not exist")
    public void shouldThrowResourceNotFoundExceptionWhenChannelDoesNotExist() {
        final User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        final List<String> channelIds = List.of("channel-id-1", "channel-id-2");

        Mockito.when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Mockito.when(subscriptionRepository.existsByUserAndChannelId(authenticatedUser, "channel-id-1")).thenReturn(true);
        Mockito.when(subscriptionRepository.existsByUserAndChannelId(authenticatedUser, "channel-id-2")).thenReturn(true);
        Mockito.when(channelService.exists("channel-id-1")).thenReturn(true);
        Mockito.when(channelService.exists("channel-id-2")).thenReturn(false);

        Assertions.assertThatThrownBy(() -> videoService.getAllByChannelIds(Mockito.mock(Pageable.class), channelIds))
                  .isInstanceOf(ResourceNotFoundException.class);

        Mockito.verify(authenticatedUserService, Mockito.times(1)).getAuthenticatedUser();
        Mockito.verify(subscriptionRepository, Mockito.times(1))
               .existsByUserAndChannelId(authenticatedUser, "channel-id-1");
        Mockito.verify(subscriptionRepository, Mockito.times(1))
               .existsByUserAndChannelId(authenticatedUser, "channel-id-2");
        Mockito.verify(channelService, Mockito.times(1)).exists("channel-id-1");
        Mockito.verify(channelService, Mockito.times(1)).exists("channel-id-2");
        Mockito.verify(videoRepository, Mockito.never()).findByChannelIdIn(Mockito.anyList(), Mockito.any());
    }
}
