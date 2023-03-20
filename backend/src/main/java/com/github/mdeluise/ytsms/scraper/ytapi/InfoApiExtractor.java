package com.github.mdeluise.ytsms.scraper.ytapi;

import com.github.mdeluise.ytsms.exception.InfoExtractionException;
import com.github.mdeluise.ytsms.scraper.InfoExtractor;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class InfoApiExtractor implements InfoExtractor {
    private final String youtubeApiKey;
    private final YouTube youTubeClient;


    public InfoApiExtractor(@Value("${youtube.key}") String youtubeApiKey)
        throws GeneralSecurityException, IOException {
        this.youtubeApiKey = youtubeApiKey;
        youTubeClient =
            new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                                null
            ).setApplicationName("ytsms").build();
    }


    @Override
    public String getChannelIDFromUsername(String username) throws InfoExtractionException {
        if (Strings.isNullOrEmpty(username)) {
            throw new InfoExtractionException("Username cannot be empty or null");
        }
        try {
            YouTube.Channels.List request = youTubeClient.channels().list("snippet");
            // 1 quota usage
            ChannelListResponse response = request.setKey(youtubeApiKey).setForUsername(username).execute();
            return response.getItems().get(0).getId();
        } catch (IOException | IndexOutOfBoundsException e) {
            throw new InfoExtractionException(e);
        }
    }
}
