package com.github.mdeluise.ytsms.video;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.Objects;

@Schema(name = "Video", description = "Represents a video.")
public class VideoDTO {
    @Schema(description = "ID of the video.", example = "6Rz2M7FmJBE")
    private String id;
    @Schema(description = "Title of the video.", example = "Introduction to Sub Feed")
    private String title;
    @Schema(description = "Published date of the video.")
    private Date publishedAt;
    @Schema(description = "Name of the channel that published the video.", example = "Proton")
    private String channelName;
    @Schema(description = "Link to the video thumbnail.", example = "https://i3.ytimg.com/vi/6Rz2M7FmJBE/hqdefault.jpg")
    private String thumbnailLink;
    @Schema(description = "View number of the video.", example = "2196")
    private Long view;


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public Date getPublishedAt() {
        return publishedAt;
    }


    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }


    public String getChannelName() {
        return channelName;
    }


    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }


    public String getThumbnailLink() {
        return thumbnailLink;
    }


    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }


    public Long getView() {
        return view;
    }


    public void setView(Long view) {
        this.view = view;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VideoDTO videoDTO = (VideoDTO) o;
        return Objects.equals(id, videoDTO.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
