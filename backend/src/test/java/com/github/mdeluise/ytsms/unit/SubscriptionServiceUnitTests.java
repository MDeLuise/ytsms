package com.github.mdeluise.ytsms.unit;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.mdeluise.ytsms.authentication.User;
import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.channel.ChannelRepository;
import com.github.mdeluise.ytsms.common.AuthenticatedUserService;
import com.github.mdeluise.ytsms.exception.InfoExtractionException;
import com.github.mdeluise.ytsms.exception.UnauthorizedException;
import com.github.mdeluise.ytsms.scraper.InfoExtractor;
import com.github.mdeluise.ytsms.scraper.VideoScraper;
import com.github.mdeluise.ytsms.subscription.Subscription;
import com.github.mdeluise.ytsms.subscription.SubscriptionRepository;
import com.github.mdeluise.ytsms.subscription.SubscriptionService;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for SubscriptionService")
public class SubscriptionServiceUnitTests {
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Mock
    private ChannelRepository channelRepository;
    @Mock
    private VideoScraper videoScraper;
    @Mock
    private InfoExtractor infoExtractor;
    @InjectMocks
    private SubscriptionService subscriptionService;


    @Test
    @DisplayName("Should return all subscriptions for the authenticated user")
    public void shouldGetAllSubscriptions() {
        final String authenticatedUsername = "testUser";
        final User authenticatedUser = new User();
        authenticatedUser.setUsername(authenticatedUsername);
        final Subscription expected1 = new Subscription();
        expected1.setId(42L);
        final List<Subscription> expected = List.of(expected1);
        Mockito.when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Mockito.when(subscriptionRepository.findAllByUser(authenticatedUser)).thenReturn(expected);

        final List<Subscription> result = subscriptionService.getAll();

        Assertions.assertThat(result).isEqualTo(expected);
        Mockito.verify(authenticatedUserService, Mockito.times(1)).getAuthenticatedUser();
        Mockito.verify(subscriptionRepository, Mockito.times(1)).findAllByUser(authenticatedUser);
    }


    @Test
    @DisplayName("Should remove a subscription by its id")
    public void shouldRemoveSubscriptionById() {
        final Long subscriptionId = 1L;
        final Subscription subscription = new Subscription();
        final User authenticatedUser = new User();
        subscription.setUser(authenticatedUser);
        Mockito.when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        Mockito.when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        subscriptionService.remove(subscriptionId);

        Mockito.verify(subscriptionRepository, Mockito.times(1)).delete(subscription);
    }


    @Test
    @DisplayName("Should return error when remove a subscription of another user")
    public void shouldReturnErrorWhenDeleteSubscriptionOtherUser() {
        final Long subscriptionId = 1L;
        final Subscription subscription = new Subscription();
        final User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        subscription.setUser(new User());
        Mockito.when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        Mockito.when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        Assertions.assertThatThrownBy(() -> subscriptionService.remove(subscriptionId))
                  .isInstanceOf(UnauthorizedException.class);

        Mockito.verify(subscriptionRepository, Mockito.never()).delete(subscription);
    }


    @Test
    @DisplayName("Should save a new subscription")
    public void shouldSaveSubscription() throws InfoExtractionException {
        final Subscription subscriptionToSave = new Subscription();
        subscriptionToSave.setChannel(new Channel());
        final User authenticatedUser = new User();
        Mockito.when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Mockito.when(channelRepository.existsById(Mockito.any())).thenReturn(false);
        Mockito.when(infoExtractor.getChannelIDFromUsername(Mockito.any())).thenReturn("channel-id-1");
        Mockito.when(channelRepository.save(Mockito.any())).thenReturn(new Channel());
        Mockito.when(subscriptionRepository.save(Mockito.any())).thenReturn(subscriptionToSave);

        final Subscription result = subscriptionService.save(subscriptionToSave);

        Assertions.assertThat(result).isEqualTo(subscriptionToSave);
        Mockito.verify(channelRepository, Mockito.times(1)).save(Mockito.any());
        Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(
            () -> Mockito.verify(videoScraper, Mockito.times(1)).saveNewVideo(Mockito.any(Set.class)));
    }


    @Test
    @DisplayName("Should save a batch of new subscriptions")
    public void shouldSaveBatchOfSubscriptions() throws InfoExtractionException {
        final Subscription subscriptionToSave1 = new Subscription();
        final Subscription subscriptionToSave2 = new Subscription();
        final Channel channel1 = new Channel();
        final Channel channel2 = new Channel();
        channel1.setId("channel-id-1");
        channel2.setId("channel-id-2");
        subscriptionToSave1.setChannel(channel1);
        subscriptionToSave2.setChannel(channel2);
        final User authenticatedUser = new User();
        Mockito.when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Mockito.when(channelRepository.existsById(Mockito.any())).thenReturn(false);
        Mockito.when(channelRepository.save(Mockito.any())).thenReturn(channel1, channel2);
        Mockito.when(subscriptionRepository.save(Mockito.any())).thenReturn(subscriptionToSave1, subscriptionToSave2);

        final List<Subscription> result = subscriptionService.saveBatch(subscriptionToSave1, subscriptionToSave2);

        Assertions.assertThat(result).containsExactlyInAnyOrder(subscriptionToSave1, subscriptionToSave2);
        Mockito.verify(channelRepository, Mockito.times(2)).save(Mockito.any());
        final ArgumentCaptor<Set<Channel>> channelsCaptor = ArgumentCaptor.forClass(Set.class);
        Mockito.verify(videoScraper, Mockito.times(1)).saveNewVideo(channelsCaptor.capture());
        final Set<Channel> capturedChannels = channelsCaptor.getValue();
        Assertions.assertThat(capturedChannels).hasSize(2);
    }


    @Test
    @DisplayName("Should save a batch of new subscriptions and retrieve only videos of new channel")
    public void shouldSaveBatchOfSubscriptionsAndRetrieveOnlyNewChannelVideo() throws InfoExtractionException {
        final Subscription subscriptionToSave1 = new Subscription();
        final Subscription subscriptionToSave2 = new Subscription();
        final Channel channel1 = new Channel();
        final Channel channel2 = new Channel();
        channel1.setId("channel-id-1");
        channel2.setId("channel-id-2");
        subscriptionToSave1.setChannel(channel1);
        subscriptionToSave2.setChannel(channel2);
        final User authenticatedUser = new User();
        Mockito.when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Mockito.when(channelRepository.existsById(Mockito.any())).thenReturn(true, false);
        Mockito.when(infoExtractor.getChannelIDFromUsername(Mockito.any())).thenReturn("channel-id-1");
        Mockito.when(channelRepository.save(Mockito.any())).thenReturn(channel2);
        Mockito.when(subscriptionRepository.save(Mockito.any())).thenReturn(subscriptionToSave1, subscriptionToSave2);

        final List<Subscription> result = subscriptionService.saveBatch(subscriptionToSave1, subscriptionToSave2);

        Assertions.assertThat(result).containsExactlyInAnyOrder(subscriptionToSave2);
        Mockito.verify(channelRepository, Mockito.times(1)).save(Mockito.any());
        final ArgumentCaptor<Set<Channel>> channelsCaptor = ArgumentCaptor.forClass(Set.class);
        Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(
            () -> Mockito.verify(videoScraper, Mockito.times(1)).saveNewVideo(channelsCaptor.capture()));
        final Set<Channel> capturedChannels = channelsCaptor.getValue();
        Assertions.assertThat(capturedChannels).hasSize(1);
    }


    @Test
    @DisplayName("Should remove all subscriptions")
    public void shouldRemoveAllSubscriptions() {
        subscriptionService.removeAll();

        Mockito.verify(subscriptionRepository, Mockito.times(1)).deleteAll();
    }
}
