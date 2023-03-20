package com.github.mdeluise.ytsms.scraper.ytapi;

import java.io.IOException;

import com.github.mdeluise.ytsms.exception.InfoExtractionException;
import com.github.mdeluise.ytsms.quota.QuotaService;
import com.github.mdeluise.ytsms.scraper.InfoExtractor;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InfoApiExtractor implements InfoExtractor {
    private final String youtubeApiKey;
    private final YouTube youTubeClient;
    private final QuotaService quotaService;
    private final Logger logger = LoggerFactory.getLogger(InfoApiExtractor.class);


    public InfoApiExtractor(@Value("${youtube.key}") String youtubeApiKey, YouTube youTubeClient,
                            QuotaService quotaService) {
        this.youtubeApiKey = youtubeApiKey;
        this.youTubeClient = youTubeClient;
        this.quotaService = quotaService;
    }


    @Override
    public String getChannelIDFromUsername(String username) {
        if (Strings.isNullOrEmpty(username)) {
            logger.error("Username cannot be empty or null");
            throw new InfoExtractionException("Username cannot be empty or null");
        }
        try {
            final YouTube.Channels.List request = youTubeClient.channels().list("snippet");
            quotaService.addToTodayQuota(1);
            final ChannelListResponse response = request.setKey(youtubeApiKey).setForUsername(username).execute();
            return response.getItems().get(0).getId();
        } catch (IOException | IndexOutOfBoundsException e) {
            logger.error("Error while fetching id for username {}", username, e);
            throw new InfoExtractionException(e);
        }
    }
}
