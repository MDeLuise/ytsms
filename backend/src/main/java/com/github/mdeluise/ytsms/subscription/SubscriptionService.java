package com.github.mdeluise.ytsms.subscription;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.mdeluise.ytsms.authentication.User;
import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.channel.ChannelRepository;
import com.github.mdeluise.ytsms.common.AuthenticatedUserService;
import com.github.mdeluise.ytsms.exception.InfoExtractionException;
import com.github.mdeluise.ytsms.exception.ResourceNotFoundException;
import com.github.mdeluise.ytsms.exception.UnauthorizedException;
import com.github.mdeluise.ytsms.scraper.InfoExtractor;
import com.github.mdeluise.ytsms.scraper.VideoScraper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final ChannelRepository channelRepository;
    private final VideoScraper videoScraper;
    private final InfoExtractor infoExtractor;
    private final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);


    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               AuthenticatedUserService authenticatedUserService, ChannelRepository channelRepository,
                               VideoScraper videoScraper, InfoExtractor infoExtractor) {
        this.subscriptionRepository = subscriptionRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.channelRepository = channelRepository;
        this.videoScraper = videoScraper;
        this.infoExtractor = infoExtractor;
    }


    public List<Subscription> getAll() {
        final User authenticatedUser = authenticatedUserService.getAuthenticatedUser();
        return subscriptionRepository.findAllByUser(authenticatedUser);
    }


    @Transactional
    public void remove(Long id) {
        final Subscription toDelete =
            subscriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        final User authenticatedUser = authenticatedUserService.getAuthenticatedUser();
        if (!authenticatedUser.equals(toDelete.getUser())) {
            logger.error("User not authorized to remove the subscription with id {}", id);
            throw new UnauthorizedException();
        }
        subscriptionRepository.delete(toDelete);
    }


    @Transactional
    public Subscription save(Subscription entityToSave) {
        entityToSave.setUser(authenticatedUserService.getAuthenticatedUser());
        if (entityToSave.getChannel().getId() == null) {
            logger.debug("Subscription does not have channel name, fetching it...");
            try {
                final String channelID = infoExtractor.getChannelIDFromUsername(entityToSave.getChannel().getName());
                entityToSave.getChannel().setId(channelID);
            } catch (InfoExtractionException e) {
                logger.error("Error while fetching channel name for subscription", e);
            }
        }
        if (!channelRepository.existsById(entityToSave.getChannel().getId())) {
            logger.debug("Channel with name {} and id {} of subscription it's not present in the db. Adding it...",
                         entityToSave.getChannel().getName(), entityToSave.getChannel().getId()
            );
            final Channel newChannel = channelRepository.save(entityToSave.getChannel());
            entityToSave.setChannel(newChannel);
            new Thread(() -> videoScraper.saveNewVideo(Set.of(newChannel))).start();
        }
        return subscriptionRepository.save(entityToSave);
    }


    @Transactional
    public List<Subscription> saveBatch(Subscription... entitiesToSave) {
        final List<Subscription> savedSubscriptions = new ArrayList<>();
        final Set<Channel> newChannels = new HashSet<>();
        for (Subscription entityToSave : entitiesToSave) {
            entityToSave.setUser(authenticatedUserService.getAuthenticatedUser());
            if (entityToSave.getChannel().getId() == null) {
                try {
                    final String channelID =
                        infoExtractor.getChannelIDFromUsername(entityToSave.getChannel().getName());
                    entityToSave.getChannel().setId(channelID);
                } catch (InfoExtractionException e) {
                    logger.error("Error while fetching username for channel with name {}",
                                 entityToSave.getChannel().getName(), e
                    );
                }
            }
            if (!channelRepository.existsById(entityToSave.getChannel().getId())) {
                logger.debug(
                    "Channel with id {} not present in the db. Adding it...", entityToSave.getChannel().getId());
                final Channel newChannel = channelRepository.save(entityToSave.getChannel());
                entityToSave.setChannel(newChannel);
                newChannels.add(newChannel);
                savedSubscriptions.add(subscriptionRepository.save(entityToSave));
            }
        }
        new Thread(() -> videoScraper.saveNewVideo(newChannels)).start();
        return savedSubscriptions;
    }


    public void removeAll() {
        subscriptionRepository.deleteAll();
    }


    public boolean isUserSubscribeToChannel(User user, String channelId) {
        return subscriptionRepository.existsByUserAndChannelId(user, channelId);
    }
}
