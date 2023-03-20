package com.github.mdeluise.ytsms.channel;

import com.github.mdeluise.ytsms.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ChannelService {
    private final ChannelRepository channelRepository;


    @Autowired
    public ChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }


    public Collection<Channel> getAll() {
        return channelRepository.findAll();
    }


    @Transactional
    public void remove(String id) {
        Channel toRemove = channelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        channelRepository.delete(toRemove);
    }


    public Channel save(Channel entityToSave) {
        return channelRepository.save(entityToSave);
    }


    public void removeAll() {
        for (Channel channel : getAll()) {
            remove(channel.getId());
        }
    }


    public Channel get(String id) {
        return channelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }


    public boolean exists(String id) {
        return channelRepository.existsById(id);
    }
}
