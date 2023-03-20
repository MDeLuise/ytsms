package com.github.mdeluise.ytsms.channel;

import java.util.Collection;

import com.github.mdeluise.ytsms.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChannelService {
    private final ChannelRepository channelRepository;
    private final Logger logger = LoggerFactory.getLogger(ChannelService.class);


    @Autowired
    public ChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }


    public Collection<Channel> getAll() {
        return channelRepository.findAll();
    }


    @Transactional
    public void remove(String id) {
        final Channel toRemove = channelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        logger.debug("Removing channel with name {} and id {}", toRemove.getName(), id);
        channelRepository.delete(toRemove);
    }


    public void remove(Channel channel) {
        channelRepository.delete(channel);
    }


    public Channel save(Channel entityToSave) {
        return channelRepository.save(entityToSave);
    }


    public void removeAll() {
        getAll().forEach(this::remove);
    }


    public Channel get(String id) {
        return channelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }


    public boolean exists(String id) {
        return channelRepository.existsById(id);
    }
}
