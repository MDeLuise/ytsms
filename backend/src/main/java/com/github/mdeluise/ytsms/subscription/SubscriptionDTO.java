package com.github.mdeluise.ytsms.subscription;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(name = "Subscription", description = "Represents a subscription.")
public class SubscriptionDTO {
    @Schema(description = "ID of the subscription.", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    @Schema(description = "Channel ID of the subscription.", example = "UCVThyXNYXDC9UhDbI8LtghQ")
    private String channelId;
    @Schema(description = "Channel Name of the subscription.", example = "Proton", accessMode = Schema.AccessMode.READ_ONLY)
    private String channelName;
    @Schema(description = "User ID of the subscription.", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;
    @Schema(
        description = "Link of the subscription channel's thumbnail.",
        example = "https://yt3.googleusercontent.com/7gH1GpiT0XGWatAc27MwqNH8PdUf1LC5zz3MS_n5yJ4MO9hoYXcS54ay5JlOfNAZNx8gdcEUig=s176-c-k-c0x00ffffff-no-rj",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String channelThumbnailLink;


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getChannelId() {
        return channelId;
    }


    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }


    public Long getUserId() {
        return userId;
    }


    public String getChannelName() {
        return channelName;
    }


    public String getChannelThumbnailLink() {
        return channelThumbnailLink;
    }


    public void setChannelThumbnailLink(String channelThumbnailLink) {
        this.channelThumbnailLink = channelThumbnailLink;
    }


    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }


    public void setUserId(Long userId) {
        this.userId = userId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubscriptionDTO that = (SubscriptionDTO) o;
        return Objects.equals(id, that.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
