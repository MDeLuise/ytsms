package com.github.mdeluise.ytsms.scraper.ytapi;

import com.github.mdeluise.ytsms.channel.ChannelService;
import com.github.mdeluise.ytsms.video.Video;
import com.github.mdeluise.ytsms.video.VideoService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.VideoContentDetails;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoStatistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;

@Component
public class VideoApiExtractorImpl implements VideoApiExtractor {
    private final String youtubeApiKey;
    private final ChannelService channelService;
    private final YouTube youTubeClient;
    private final VideoService videoService;


    public VideoApiExtractorImpl(@Value("${youtube.key}") String youtubeApiKey, ChannelService channelService,
                                 VideoService videoService) throws GeneralSecurityException, IOException {
        this.youtubeApiKey = youtubeApiKey;
        this.channelService = channelService;
        this.videoService = videoService;
        youTubeClient =
            new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                                null
            ).setApplicationName("ytsms").build();
    }


    /*
    This works and it's simple, but consumes 100 quota per channel + 1 quota for each new video retrieved

        @Override
        public Collection<Video> getVideo(String channelId) throws IOException {
            ArrayList<Video> channelVideo = new ArrayList<>();
            YouTube.Search.List request = youTubeClient.search().list("snippet");
            SearchListResponse response =
                request.setKey(youtubeApiKey).setChannelId(channelId).setMaxResults(50L).execute();
            for (SearchResult video : response.getItems()) {
                SearchResultSnippet videoSnippet = video.getSnippet();
                setChannelNameIfNotAlreadyDone(channelId, videoSnippet.getChannelTitle());
                Video videoToSave = new Video();
                Channel channel =
                    channelRepository.findById(channelId).orElseThrow(() -> new ResourceNotFoundException("id",
                    channelId));
                videoToSave.setChannel(channel);
                videoToSave.setId(video.getId().getVideoId());
                videoToSave.setTitle(videoSnippet.getTitle());
                videoToSave.setPublishedAt(Date.from(Instant.parse(video.getSnippet().getPublishedAt().toString())));
                videoToSave.setThumbnailLink(videoSnippet.getThumbnails().getHigh().getUrl());
                if (!videoService.exists(videoToSave.getId())) {
                    setViewCountAndDuration(videoToSave);
                }
                channelVideo.add(videoToSave);
            }
            return channelVideo;
        }


        private void setChannelNameIfNotAlreadyDone(String channelId, String channelName) {
            Channel channel =
                channelRepository.findById(channelId).orElseThrow(() -> new ResourceNotFoundException("id",
                                                                                                      channelId));
            if (channel.getName() != null) {
                return;
            }
            channel.setName(channelName);
            channelRepository.save(channel);
        }


        public void setViewCountAndDuration(Video video) throws IOException {
            YouTube.Videos.List request = youTubeClient.videos().list("contentDetails,statistics");
            VideoListResponse response = request.setId(video.getId()).setKey(youtubeApiKey).setMaxResults(1L)
                                                .execute();
            VideoStatistics fetchedVideoStatistics = response.getItems().get(0).getStatistics();
            VideoContentDetails fetchedVideoContentDetails = response.getItems().get(0).getContentDetails();
            video.setView(fetchedVideoStatistics.getViewCount().longValue());
            video.setSecondsDuration(Duration.parse(fetchedVideoContentDetails.getDuration()).getSeconds());
        }
     */


    // This is more complicated but it consumes less quota
    // Quota usage: 1 + 1 * #channels + 1 * #new_videos
    @Override
    public Collection<Video> getVideo(String... channelIds) throws IOException {
        String joinedChannelIds = String.join(",", channelIds);
        YouTube.Channels.List channelsRequest = youTubeClient.channels().list("contentDetails,snippet");
        // 1 quota usage
        ChannelListResponse response =
            channelsRequest.setKey(youtubeApiKey).setMaxResults(50L).setId(joinedChannelIds).execute();
        Collection<String> uploadedUrls = new HashSet<>();
        for (Channel channel : response.getItems()) {
            updateChannel(channel);
            String uploadsUrl = channel.getContentDetails().getRelatedPlaylists().getUploads();
            uploadedUrls.add(uploadsUrl);
        }

        Collection<Video> newVideo = new HashSet<>();
        for (String uploadedUrl : uploadedUrls) {
            YouTube.PlaylistItems.List videoUploadedRequest =
                youTubeClient.playlistItems().list("snippet,contentDetails");
            // 1 quota usage
            PlaylistItemListResponse videoUploadedResponse =
                videoUploadedRequest.setPlaylistId(uploadedUrl).setMaxResults(50L).setKey(youtubeApiKey).execute();
            for (PlaylistItem uploadedVideo : videoUploadedResponse.getItems()) {
                PlaylistItemSnippet videoSnippet = uploadedVideo.getSnippet();
                if (videoService.exists(videoSnippet.getResourceId().getVideoId())) {
                    continue;
                }
                Video video = new Video();
                video.setId(videoSnippet.getResourceId().getVideoId());
                video.setTitle(videoSnippet.getTitle());
                video.setChannel(channelService.get(videoSnippet.getChannelId()));
                video.setPublishedAt(Date.from(Instant.parse(videoSnippet.getPublishedAt().toString())));
                video.setThumbnailLink(videoSnippet.getThumbnails().getHigh().getUrl());
                setViewCountAndDuration(video);
                newVideo.add(video);
            }
        }
        return newVideo;
    }


    public void setViewCountAndDuration(Video video) throws IOException {
        YouTube.Videos.List request = youTubeClient.videos().list("contentDetails,statistics");
        // 1 quota usage
        VideoListResponse response = request.setId(video.getId()).setKey(youtubeApiKey).setMaxResults(1L).execute();
        VideoStatistics fetchedVideoStatistics = response.getItems().get(0).getStatistics();
        VideoContentDetails fetchedVideoContentDetails = response.getItems().get(0).getContentDetails();
        video.setView(fetchedVideoStatistics.getViewCount().longValue());
        video.setSecondsDuration(Duration.parse(fetchedVideoContentDetails.getDuration()).getSeconds());
    }


    private void updateChannel(Channel channel) {
        ChannelSnippet channelSnippet = channel.getSnippet();
        com.github.mdeluise.ytsms.channel.Channel savedChannel = channelService.get(channel.getId());
        String thumbnailUrl = channel.getSnippet().getThumbnails().getMedium().getUrl();
        boolean savedChannelModified = false;
        if (!thumbnailUrl.equals(savedChannel.getThumbnailLink())) {
            savedChannel.setThumbnailLink(channelSnippet.getThumbnails().getMedium().getUrl());
            savedChannelModified = true;
        }
        if (!channelSnippet.getTitle().equals(savedChannel.getName())) {
            savedChannel.setName(channelSnippet.getTitle());
            savedChannelModified = true;
        }
        if (savedChannelModified) {
            channelService.save(savedChannel);
        }
    }
}
