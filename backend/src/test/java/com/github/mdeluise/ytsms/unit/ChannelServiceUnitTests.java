package com.github.mdeluise.ytsms.unit;

import java.util.List;
import java.util.Optional;

import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.channel.ChannelRepository;
import com.github.mdeluise.ytsms.channel.ChannelService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DisplayName("Unit tests for ChannelService")
public class ChannelServiceUnitTests {
    @Mock
    private ChannelRepository channelRepository;
    @InjectMocks
    private ChannelService channelService;


    @Test
    @DisplayName("Should return all channels")
    public void shouldGetAllChannels() {
        final Channel expected1 = new Channel();
        expected1.setId("channel-id-1");
        final Channel expected2 = new Channel();
        expected1.setId("channel-id-2");
        final List<Channel> expected = List.of(expected1, expected2);
        Mockito.when(channelRepository.findAll()).thenReturn(expected);

        Assertions.assertThat(channelService.getAll()).containsExactlyInAnyOrder(expected.toArray(Channel[]::new));
        Mockito.verify(channelRepository, Mockito.times(1)).findAll();
    }


    @Test
    @DisplayName("Should remove a channel by its id")
    public void shouldRemoveChannelById() {
        final String channelId = "channel-id-1";
        final Channel channel = new Channel();
        Mockito.when(channelRepository.findById(channelId)).thenReturn(Optional.of(channel));

        channelService.remove(channelId);

        Mockito.verify(channelRepository, Mockito.times(1)).delete(channel);
    }


    @Test
    @DisplayName("Should save a new channel")
    public void shouldSaveChannel() {
        final Channel channelToSave = new Channel();
        channelToSave.setId("channel-id-1");
        Mockito.when(channelRepository.save(channelToSave)).thenReturn(channelToSave);

        Assertions.assertThat(channelService.save(channelToSave)).isEqualTo(channelToSave);
        Mockito.verify(channelRepository, Mockito.times(1)).save(channelToSave);
    }


    @Test
    @DisplayName("Should remove all channels")
    public void shouldRemoveAllChannels() {
        final String channelId1 = "channel-id-1";
        final String channelId2 = "channel-id-2";
        final Channel toDelete1 = new Channel();
        toDelete1.setId(channelId1);
        final Channel toDelete2 = new Channel();
        toDelete2.setId(channelId2);
        final List<Channel> toDelete = List.of(toDelete1, toDelete2);
        Mockito.when(channelRepository.findAll()).thenReturn(toDelete);

        channelService.removeAll();

        final ArgumentCaptor<Channel> channelCaptor = ArgumentCaptor.forClass(Channel.class);
        Mockito.verify(channelRepository, Mockito.times(2)).delete(channelCaptor.capture());
        final List<Channel> capturedChannels = channelCaptor.getAllValues();
        Assertions.assertThat(capturedChannels).extracting(Channel::getId)
                  .containsExactlyInAnyOrder(channelId1, channelId2);
    }


    @Test
    @DisplayName("Should get channel by id")
    public void shouldGetChannelById() {
        final String channelId = "testId";
        final Channel expected = new Channel();
        expected.setId(channelId);
        Mockito.when(channelRepository.findById(channelId)).thenReturn(Optional.of(expected));

        Assertions.assertThat(channelService.get(channelId)).isEqualTo(expected);
        Mockito.verify(channelRepository, Mockito.times(1)).findById(channelId);
    }


    @Test
    @DisplayName("Should check if channel exists by id")
    public void shouldCheckIfChannelExistsById() {
        final String channelId = "testId";
        Mockito.when(channelRepository.existsById(channelId)).thenReturn(true);

        Assertions.assertThat(channelService.exists(channelId)).isTrue();
        Mockito.verify(channelRepository, Mockito.times(1)).existsById(channelId);
    }
}
