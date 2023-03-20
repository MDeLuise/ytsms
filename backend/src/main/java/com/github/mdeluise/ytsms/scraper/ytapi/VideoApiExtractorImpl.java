package com.github.mdeluise.ytsms.scraper.ytapi;

import java.io.IOException;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.mdeluise.ytsms.channel.ChannelService;
import com.github.mdeluise.ytsms.quota.QuotaService;
import com.github.mdeluise.ytsms.video.Video;
import com.github.mdeluise.ytsms.video.VideoService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VideoApiExtractorImpl implements VideoApiExtractor {
    private final String youtubeApiKey;
    private final ChannelService channelService;
    private final YouTube youTubeClient;
    private final VideoService videoService;
    private final QuotaService quotaService;
    private final Logger logger = LoggerFactory.getLogger(VideoApiExtractorImpl.class);


    public VideoApiExtractorImpl(@Value("${youtube.key}") String youtubeApiKey, YouTube youTubeClient,
                                 ChannelService channelService, VideoService videoService, QuotaService quotaService) {
        this.youtubeApiKey = youtubeApiKey;
        this.channelService = channelService;
        this.videoService = videoService;
        this.youTubeClient = youTubeClient;
        this.quotaService = quotaService;
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
    // Quota usage: 1 + 1 * (#channels / 50) + 1 * #new_videos
    @Override
    public Collection<Video> getVideo(String... channelIds) throws IOException {
        final Collection<Channel> channelInfo = fetchChannelInfo(channelIds);

        final Collection<String> uploadedUrls = new HashSet<>();
        for (Channel channel : channelInfo) {
            updateChannel(channel);
            final String uploadsUrl = channel.getContentDetails().getRelatedPlaylists().getUploads();
            uploadedUrls.add(uploadsUrl);
        }

        final Collection<Video> newVideo = new HashSet<>();
        for (String uploadedUrl : uploadedUrls) {
            final Collection<PlaylistItem> uploadedVideos = fetchUploadedVideo(uploadedUrl);
            for (PlaylistItem uploadedVideo : uploadedVideos) {
                final PlaylistItemSnippet videoSnippet = uploadedVideo.getSnippet();
                if (videoService.exists(videoSnippet.getResourceId().getVideoId())) {
                    continue;
                }
                final Video video = createVideo(uploadedVideo);
                newVideo.add(video);
            }
        }
        return newVideo;
    }


    // Split needed because API return "The request specifies an invalid filter parameter"
    // if more than 50 items are provided in the id.
    private Collection<Channel> fetchChannelInfo(String... channelIds) throws IOException {
        final Set<Channel> result = new HashSet<>();
        final YouTube.Channels.List request = prepareChannelInfoRequest();
        final Collection<String> joinedChannels = joinChannelsIntoString(50, channelIds);

        for (String ids : joinedChannels) {
            quotaService.addToTodayQuota(1);
            final ChannelListResponse response;
            try {
                logger.debug("Fetching channel info for ids {}...", ids);
                response = request.setKey(youtubeApiKey).setMaxResults(50L).setId(ids).execute();
            } catch (IOException e) {
                logger.error("Error while retrieving channels info for {}", ids, e);
                throw e;
            }
            result.addAll(response.getItems());
        }
        return result;
    }


    private YouTube.Channels.List prepareChannelInfoRequest() throws IOException {
        final YouTube.Channels.List channelsRequest;
        try {
            channelsRequest = youTubeClient.channels().list("contentDetails,snippet");
        } catch (IOException e) {
            logger.error("Error while creating channels info request", e);
            throw e;
        }
        return channelsRequest;
    }


    private Collection<String> joinChannelsIntoString(int batchSize, String... channelIds) {
        final List<String> resultStrings = new ArrayList<>();
        final int totalElements = channelIds.length;

        for (int i = 0; i < totalElements; i += batchSize) {
            final int endIndex = Math.min(i + batchSize, totalElements);
            String[] batch = Arrays.copyOfRange(channelIds, i, endIndex);
            String joinedString = String.join(",", batch);
            resultStrings.add(joinedString);
        }
        return resultStrings;
    }


    private Collection<PlaylistItem> fetchUploadedVideo(String uploadUrl) throws IOException {
        final YouTube.PlaylistItems.List request = prepareVideoUploadedRequest();
        quotaService.addToTodayQuota(1);
        final PlaylistItemListResponse videoUploadedResponse;
        logger.debug("Fetching uploaded video for url {}...", uploadUrl);
        try {
            videoUploadedResponse =
                request.setPlaylistId(uploadUrl).setMaxResults(50L).setKey(youtubeApiKey).execute();
        } catch (IOException e) {
            logger.error("Error while retrieving video from url {}", uploadUrl, e);
            throw e;
        }
        return videoUploadedResponse.getItems();
    }


    private YouTube.PlaylistItems.List prepareVideoUploadedRequest() throws IOException {
        final YouTube.PlaylistItems.List videoUploadedRequest;
        try {
            videoUploadedRequest = youTubeClient.playlistItems().list("snippet,contentDetails");
        } catch (IOException e) {
            logger.error("Error while creating video uploaded request", e);
            throw e;
        }
        return videoUploadedRequest;
    }


    private Video createVideo(PlaylistItem playlistItem) throws IOException {
        final PlaylistItemSnippet videoSnippet = playlistItem.getSnippet();
        final Video video = new Video();
        video.setId(videoSnippet.getResourceId().getVideoId());
        video.setTitle(videoSnippet.getTitle());
        video.setChannel(channelService.get(videoSnippet.getChannelId()));
        video.setPublishedAt(Date.from(Instant.parse(videoSnippet.getPublishedAt().toString())));
        video.setThumbnailLink(videoSnippet.getThumbnails().getHigh().getUrl());
        setViewCountAndDuration(video);
        return video;
    }


    public void setViewCountAndDuration(Video video) throws IOException {
        final YouTube.Videos.List request;
        try {
            request = youTubeClient.videos().list("contentDetails,statistics");
        } catch (IOException e) {
            logger.error("Error while creating video list for {}", video.getId(), e);
            throw e;
        }
        quotaService.addToTodayQuota(1);
        final VideoListResponse response;
        logger.debug("Fetching stats for video with id {} and title {}...", video.getId(), video.getTitle());
        try {
            response = request.setId(video.getId()).setKey(youtubeApiKey).setMaxResults(1L).execute();
        } catch (IOException e) {
            logger.error("Error while retrieving info for video {}", video.getId(), e);
            throw e;
        }
        final VideoStatistics fetchedVideoStatistics = response.getItems().get(0).getStatistics();
        final VideoContentDetails fetchedVideoContentDetails = response.getItems().get(0).getContentDetails();
        video.setView(fetchedVideoStatistics.getViewCount().longValue());
        video.setSecondsDuration(Duration.parse(fetchedVideoContentDetails.getDuration()).getSeconds());
    }


    private void updateChannel(Channel channel) {
        logger.debug("Updating channel with title {} and id {}...", channel.getSnippet().getTitle(), channel.getId());
        final ChannelSnippet channelSnippet = channel.getSnippet();
        final com.github.mdeluise.ytsms.channel.Channel savedChannel = channelService.get(channel.getId());
        final String thumbnailUrl = channel.getSnippet().getThumbnails().getMedium().getUrl();
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
