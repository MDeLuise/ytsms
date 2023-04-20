package com.github.mdeluise.ytsms.subscription;

import com.github.mdeluise.ytsms.authentication.User;
import com.github.mdeluise.ytsms.authentication.UserService;
import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.channel.ChannelRepository;
import com.github.mdeluise.ytsms.exception.InfoExtractionException;
import com.github.mdeluise.ytsms.exception.ResourceNotFoundException;
import com.github.mdeluise.ytsms.exception.UnauthorizedException;
import com.github.mdeluise.ytsms.scraper.InfoExtractor;
import com.github.mdeluise.ytsms.scraper.VideoScraper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;
    private final ChannelRepository channelRepository;
    private final VideoScraper videoScraper;
    private final InfoExtractor infoExtractor;


    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserService userService,
                               ChannelRepository channelRepository, VideoScraper videoScraper,
                               InfoExtractor infoExtractor) {
        this.subscriptionRepository = subscriptionRepository;
        this.userService = userService;
        this.channelRepository = channelRepository;
        this.videoScraper = videoScraper;
        this.infoExtractor = infoExtractor;
    }


    public List<Subscription> getAll() {
        User authenticatedUser = getAuthenticatedUser();
        return subscriptionRepository.findAllByUser(authenticatedUser);
    }


    @Transactional
    public void remove(Long id) {
        Subscription toDelete =
            subscriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        User authenticatedUser = getAuthenticatedUser();
        if (!authenticatedUser.equals(toDelete.getUser())) {
            throw new UnauthorizedException();
        }
        subscriptionRepository.delete(toDelete);
    }


    @Transactional
    public Subscription save(Subscription entityToSave) {
        entityToSave.setUser(getAuthenticatedUser());
        if (entityToSave.getChannel().getId() == null) {
            try {
                String channelID = infoExtractor.getChannelIDFromUsername(entityToSave.getChannel().getName());
                entityToSave.getChannel().setId(channelID);
            } catch (InfoExtractionException ignored) {
            }
        }
        if (!channelRepository.existsById(entityToSave.getChannel().getId())) {
            Channel newChannel = channelRepository.save(entityToSave.getChannel());
            entityToSave.setChannel(newChannel);
            new Thread(() -> videoScraper.saveNewVideo(newChannel)).start();
        }
        return subscriptionRepository.save(entityToSave);
    }


    public void removeAll() {
        subscriptionRepository.deleteAll();
    }


    private User getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String username = authentication.getName();
        return userService.get(username);
    }
}
