package com.github.mdeluise.ytsms.subscription.service;

import com.github.mdeluise.ytsms.TestEnvironment;
import com.github.mdeluise.ytsms.authentication.User;
import com.github.mdeluise.ytsms.authentication.UserService;
import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.channel.ChannelRepository;
import com.github.mdeluise.ytsms.exception.ResourceNotFoundException;
import com.github.mdeluise.ytsms.scraper.VideoScraper;
import com.github.mdeluise.ytsms.subscription.Subscription;
import com.github.mdeluise.ytsms.subscription.SubscriptionRepository;
import com.github.mdeluise.ytsms.subscription.SubscriptionService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@WithMockUser(username = "user")
@Import(TestEnvironment.class)
public class SubscriptionServiceTest {
    @Mock
    SubscriptionRepository subscriptionRepository;
    @Mock
    ChannelRepository channelRepository;
    @Mock
    UserService userService;
    @Mock
    VideoScraper videoScraper;
    @InjectMocks
    SubscriptionService subscriptionService;


    @Test
    void whenSaveSubscription_thenReturnSubscription() {
        User user = new User();
        user.setId(0L);
        Channel channel = new Channel();
        channel.setId("0");
        Subscription toSave = new Subscription();
        toSave.setId(0L);
        toSave.setChannel(channel);
        toSave.setUser(user);
        Mockito.when(subscriptionRepository.save(toSave)).thenReturn(toSave);
        Mockito.when(userService.get("user")).thenReturn(user);
        Mockito.when(channelRepository.save(channel)).thenReturn(channel);

        Assertions.assertThat(subscriptionService.save(toSave)).isSameAs(toSave);
    }


    @Test
    void whenGetAllSubscriptions_thenReturnAllSubscriptions() {
        Subscription toGet1 = new Subscription();
        toGet1.setId(0L);
        toGet1.setChannel(new Channel());
        Subscription toGet2 = new Subscription();
        toGet2.setId(1L);
        toGet2.setChannel(new Channel());

        List<Subscription> allSubscriptions = List.of(toGet1, toGet2);
        Mockito.when(subscriptionRepository.findAllByUser(Mockito.any())).thenReturn(allSubscriptions);

        Assertions.assertThat(subscriptionService.getAll()).isSameAs(allSubscriptions);
    }


    @Test
    void givenSubscription_whenDeleteSubscription_thenDeleteSubscription() {
        User user = new User();
        user.setId(0L);
        Channel channel = new Channel();
        channel.setId("0");
        Subscription subscription = new Subscription();
        subscription.setChannel(channel);
        subscription.setUser(user);
        Mockito.when(subscriptionRepository.findById(0L)).thenReturn(Optional.of(subscription));
        Mockito.when(userService.get("user")).thenReturn(user);
        subscriptionService.remove(0L);
        Mockito.verify(subscriptionRepository, Mockito.times(1)).delete(subscription);
    }


    @Test
    void whenDeleteNonExistingSubscription_thenError() {
        User user = new User();
        user.setId(0L);
        Mockito.when(userService.get("user")).thenReturn(user);
        Mockito.when(subscriptionRepository.findById(0L)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> subscriptionService.remove(0L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
